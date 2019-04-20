package impl;


import annotation.RefreshHotPost;
import interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//todo 配置化
@Slf4j
public class DefaultHotPostHandler<E> extends AbstractHotPostHandler<E> {

    //增量更新阀值
    private static final int DEFAULT_CHANGE_TASK_THRESHOLD = 100;
    //刷新时间
    private static final String REFRESH_TIME = "2:00:00";

    //上次刷新时间
    private static Date lastRefreshTime = null;
    //写入数据库线程
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();
    //存储任务队列
    private static ConcurrentLinkedQueue<Integer> taskQueue = new ConcurrentLinkedQueue();
    //浏览量计算
    private ViewCounter viewCounter = new DefaultViewCounter();
    //热度变更锁
    private ReentrantReadWriteLock changeLock = new ReentrantReadWriteLock();
    //存储恢复数据路径
    private String path = null;

    public DefaultHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache,
                                 Fetcher fetcher) {
        super(viewCache, hotPointCache, fetcher);
        executorService.execute(new SaveTask());
    }

    public DefaultHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache, Fetcher fetcher, ConcurrentHashMap indexCache, ConcurrentHashMap hotPostCache) {
        super(viewCache, hotPointCache, fetcher);
        this.index = indexCache;
        this.topPost = hotPostCache;
    }

    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getHotPoint(int pid) {
        HotPoint hotPoint = hotPointCache.get(pid);
        return hotPoint == null ? 0 : hotPoint.getTotal();
    }

    protected void computeViewNum(int pid, Object object) {
        if (viewCache.putIfAbsent((String) viewCounter.getID(pid, fetcher), 1) == null) {
            //需要计算浏览量
            updateTopPost(pid, hotPointCache.createOrUpdate(pid, 1l));
        }
    }

    protected void addReplyNum(int pid) {
        updateTopPost(pid, hotPointCache.createOrUpdate(pid, 5l));
    }

    protected void process(JoinPoint point) {
        Object[] args = point.getArgs();
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method m = ms.getMethod();
        String type = "";
        Annotation[] annotations = m.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.annotationType() == RefreshHotPost.class) {
                type = ((RefreshHotPost) annotation).value();
            }
        }
        Signature signature = point.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        //2.最关键的一步:通过这获取到方法的所有参数名称的字符串数组
        String[] parameterNames = methodSignature.getParameterNames();
        //3.通过你需要获取的参数名称的下标获取到对应的值
        int index = Arrays.asList(parameterNames).indexOf("pid");
        int pid = -1;
        if (index != -1) {
            pid = (int) args[index];
        }
        if (pid != -1) {
            //log.info("准备计算热度：type：{} pid:{}", type, pid);
            changeLock.readLock().lock();
            switch (type) {
                case "view":
                    computeViewNum(pid, null);
                    break;
                case "reply":
                    addReplyNum(pid);
                    break;
                case "delPost":
                    try {
                        del(pid);
                    } catch (Exception e) {
                        log.error("删除帖子后尝试更新数据库错误，错误信息：{}", e.getMessage());
                    }
                    break;
                case "delReply":
                    try {
                        delReply(pid);
                    } catch (Exception e) {
                        log.error("删除回复后尝试更新数据库错误，错误信息：{}", e.getMessage());
                    }
                    break;
                default:
                    changeLock.readLock().unlock();
                    return;
            }
            changeLock.readLock().unlock();
            change(pid);
        }

    }

    /**
     * 删除回复：修改增量
     *
     * @param pid
     */
    protected void delReply(int pid) {
        hotPointCache.delReply(pid);
    }

    private void change(int pid) {
        if (hotPointCache.get(pid).getChangNum() >= DEFAULT_CHANGE_TASK_THRESHOLD) {
            addSaveTask(pid);
        }
    }

    /**
     * 判断pid是否影响了热帖排行，并且删除对应缓存
     *
     * @param pid
     */
    protected void del(int pid) {
        lock.writeLock().lock();
        topPost.remove(pid);
        index.remove(pid);
        //重置门槛，仅当热帖量超出10时再计算
        //筛选当前热度第十名：性能消耗可能更大，因为遍历的是当时全部的帖子集合
        lowestHotpoint = 0;
        lock.writeLock().unlock();
        hotPointCache.remove(pid);
        viewCache.removeLike(pid);

    }

    /**
     * @param pid
     * @param hotpoint
     */
    protected void updateTopPost(int pid, long hotpoint) {
        //实时淘汰
        if (lowestHotpoint < hotpoint) {
            lock.readLock().lock();
            //二次判断，判断得到锁后是否符合条件
            if (lowestHotpoint > hotpoint) {
                lock.readLock().unlock();
                return;
            }
            log.info("正在更新TopPost,pid:{}-hotpoint:{}", pid, hotpoint);
            if (index.size() == 0) {
                index.put(pid, hotpoint);
                topPost.put(pid, fetcher.getInfo(pid));
                lock.readLock().unlock();

                return;
            }
            /**
             * compute()无论在不在都更新，返回最新值
             * computeifabsent 仅在不在是时候创建，创建后返回创建时指定的值
             * computeifPresent 仅在在的时候更新，没有的话不创建，返回null，有的话返回最新的值
             */
            if (index.computeIfPresent(pid, (k, v) -> hotpoint) == null) {
                //新晋热帖
                //没超出
                log.info("新晋热帖");
                index.put(pid, hotpoint);
                if (index.size() <= 10) {
                    topPost.put(pid, fetcher.getInfo(pid));
                    lock.readLock().unlock();
                    return;
                }
                //超出了
                Iterator<Map.Entry<Integer, Long>> entries = index.entrySet().iterator();
                while (entries.hasNext()) {
                    {
                        Map.Entry<Integer, Long> entry = entries.next();
                        if (entry.getValue() == lowestHotpoint) {
                            //删除原有的
                            topPost.remove(entry.getKey());
                            index.remove(entry.getKey());
                        } else if (lowestHotpoint > entry.getValue()) {
                            //更新门槛
                            lowestHotpoint = entry.getValue();
                        }
                    }
                }
            }
            lock.readLock().unlock();
        }

    }

    public List<E> getTopPost() {
        List list = new ArrayList();
        topPost.forEachValue(1, v -> list.add(v));
        return list;
    }

    @Override
    public Map<Integer, Long> getTopPostHotPoint() {
        return index;
    }

    public void addCycleSaveTask(int period, TimeUnit unit) {
        RefreshNow();
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = getTimeMillis(REFRESH_TIME) - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        scheduledExecutorService.scheduleAtFixedRate(new DefaultRefresh(), initDelay, period, unit);
    }

    private void RefreshNow() {
        //fixme 保存到数据库中，每次运行读出来
/*        if(lastRefreshTime==null){
            refresh();
        }*/
    }

    public void addCycleSaveTaskForReload(int period, TimeUnit unit, String path) {
        if (path == null) {
            return;
        }
        this.path = path;
        scheduledExecutorService.scheduleAtFixedRate(new SaveForReloadTask(), 0, period, unit);
    }

    private void addSaveTask(Set<Integer> tasks) {
        taskQueue.addAll(tasks);
    }

    private void addSaveTask(int pid) {
        taskQueue.offer(pid);
    }

    /**
     * 刷新缓存，重置热帖榜
     */
    private void refresh() {
        /**
         * 清空顺序很关键
         * viewcache→hotpointcache 所以先清空hotpointcache不会引起数据的不一致性
         */
        lock.writeLock().lock();
        if (dataSaver != null) {
            addSaveTask((hotPointCache.getMap()).keySet());
        } else {
            hotPointCache = hotPointCache.refresh();
        }
        viewCache = viewCache.refresh();
        /**
         * toppost不需要刷新，保留昨天的，仅把热度统一更新为固定值
         * 这里加锁对应 缓存更新到索引中，保证了一致性
         */
        index.forEach((k, v) -> index.compute(k, (x, y) -> defaultHotpoint));
        //重置门槛，仅当热帖量超出10时再计算
        lowestHotpoint = 0;
        lock.writeLock().unlock();
        lastRefreshTime = new Date();
    }

    /**
     * 定时刷新线程
     */
    class DefaultRefresh implements Runnable {
        @Override
        public void run() {
            refresh();
        }
    }

    /**
     * 消费者线程，实时更新热帖版
     */
    private class SaveTask implements Runnable {
        @Override
        public void run() {
            final int SLEEP_STEP = 100;
            final int MAX_SLEEP = 500;
            int sleepTime = SLEEP_STEP;
            while (true) {
                Integer pid = taskQueue.poll();
                while (pid != null) {
                    //一旦出现任务，重置等待延时
                    sleepTime = SLEEP_STEP;
                    if (hotPointCache.get(pid).getChangNum() < DEFAULT_CHANGE_TASK_THRESHOLD) {
                        continue;
                    }
                    changeLock.writeLock().lock();
                    dataSaver.save(pid, hotPointCache.get(pid));
                    log.info("保存浏览记录：pid:{} hotPoint:{}", pid, hotPointCache.get(pid).toString());
                    hotPointCache.reset(pid);
                    changeLock.writeLock().unlock();
                    pid = taskQueue.poll();
                }
                try {
                    Thread.sleep(sleepTime < MAX_SLEEP ? sleepTime += SLEEP_STEP : sleepTime);
                } catch (InterruptedException e) {
                    log.error("保存线程挂掉：{}", e.getMessage());
                }
            }
        }
    }

    /**
     * 定时保存线程
     */
    private class SaveForReloadTask implements Runnable {
        @Override
        public void run() {
            LoadRecord.saveRecord(viewCache, hotPointCache, index, topPost, path);
        }
    }
}
