package impl;


import annotation.RefreshHotPost;
import domain.HotPoint;
import domain.SaveEntity;
import interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import util.DateUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * 默认的热度计算器
 * 1.viewCache:用户浏览记录缓存
 * 2.hotpointCache 热度缓存
 * 3.indexCache: 热帖索引
 * 4.hotpostCache ：热帖热度
 *
 * @param <E> 帖子类型
 */
@Slf4j
public class DefaultHotPostHandler<E> extends AbstractHotPostHandler<E> {

    //上次刷新时间
    //private static Date lastRefreshTime = null;
    //写入数据库线程
    private static ExecutorService consumerThread = Executors.newFixedThreadPool(5);
    //存储任务队列
    private static ConcurrentLinkedQueue<SaveEntity> taskQueue = new ConcurrentLinkedQueue<>();
    //增量更新阀值
    @Value("${hotPoint.threshold}")
    private int updateThreshold;
    //刷新时间
    @Value("${hotPoint.refreshTime}")
    private String refreshTime;
    @Value("${hotPoint.saveTaskThreadNum}")
    private int saveTaskThreadNum;
    //浏览量计算
    private ViewCounter viewCounter = new DefaultViewCounter();
    //存储恢复数据路径
    private String path = null;

    public DefaultHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache,
                                 Fetcher fetcher) {
        super(viewCache, hotPointCache, fetcher);
    }

    public DefaultHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache,
                                 Fetcher<E> fetcher, ConcurrentHashMap<Integer, Long> indexCache,
                                 ConcurrentHashMap<Integer, E> hotPostCache) {
        super(viewCache, hotPointCache, fetcher);
        if (Objects.isNull(indexCache)) {
            this.index = new ConcurrentHashMap<>(10);
            topPost = new ConcurrentHashMap<>(10);
        } else {
            this.index = indexCache;
            if (Objects.isNull(hotPostCache)) {
                this.topPost = new ConcurrentHashMap<>(10);
            } else {
                topPost = hotPostCache;
            }
        }

    }

    private static long getTimeMillis(String time) {
        String[] t = time.split(":", 3);
        int hour;
        int minute;
        int second;
        if (t.length == 3) {
            hour = Integer.parseInt(t[0]);
            minute = Integer.parseInt(t[1]);
            second = Integer.parseInt(t[2]);
        } else {
            //spring @value机制有问题//大于12点自动转换为毫秒
            int timeInt = Integer.parseInt(t[0]);
            hour = timeInt / 60 / 60;
            minute = timeInt % 3600 / 60;
            second = timeInt % 60;
        }

        String timeString = hour + ":" + minute + ":" + second;
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
        try {
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + timeString);
            return curDate.getTime();
        } catch (ParseException e) {
            log.error("日期计算错误:{}", e.getMessage());
        }
        return 0;
    }

    @Override
    public HotPoint getHotPoint(int pid) {
        return hotPointCache.get(pid);
    }

    @Override
    protected void computeViewNum(int pid) {
        if (viewCache.putIfAbsent((String) viewCounter.getID(pid, fetcher), 1) == null) {
            //需要计算浏览量
            updateTopPost(pid, hotPointCache.createOrUpdate(pid, 1L));
        }
    }

    @Override
    protected void addReplyNum(int pid) {
        updateTopPost(pid, hotPointCache.createOrUpdate(pid, 4L));
    }

    @Override
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
        int pid = -1;
        int index = Arrays.asList(parameterNames).indexOf("pid");
        if (index != -1) {
            pid = (int) args[index];
        }
        if (index == -1) {
            //添加回复也会找不到
            index = Arrays.asList(parameterNames).indexOf("reply");
            if (index != -1) {
                pid = fetcher.getPID(args[index]);
            }

        }
        if (pid != -1) {
            log.info("准备计算热度：type：{} pid:{}", type, pid);
            switch (type) {
                case "view":
                    computeViewNum(pid);
                    checkUpdate(pid);
                    break;
                case "reply":
                    addReplyNum(pid);
                    break;
                case "delPost":
                    del(pid);
                    break;
                case "delReply":
                    delReply(pid);
                    break;
                default:
                    break;
            }
        }

    }

    /**
     * 删除回复：修改增量
     *
     * @param pid
     */
    protected void delReply(int pid) {
        log.info("删除回复，减少热度");
        hotPointCache.delReply(pid);
    }

    /**
     * 检查是否需要更新数据库
     *
     * @param pid
     */
    private void checkUpdate(int pid) {
        if (hotPointCache.get(pid).getViewIncrement() >= updateThreshold) {
            addSaveTask(pid);
        }
    }

    /**
     * 删除帖子后的处理
     * 判断pid是否影响了热帖排行，并且删除对应缓存
     *
     * @param pid
     */
    @Override
    protected void del(int pid) {
        lock.writeLock().lock();
        topPost.remove(pid);
        index.remove(pid);
        //重置门槛，仅当热帖量超出10时再计算
        //筛选当前热度第十名：性能消耗可能更大，因为遍历的是当时全部的帖子集合
        lock.writeLock().unlock();
        hotPointCache.remove(pid);
        viewCache.removeLike(pid);
    }

    /**
     * @param pid
     * @param hotpoint
     */
    @Override
    protected void updateTopPost(int pid, long hotpoint) {
        //实时淘汰
        if (getMinHotPointOfLock() < hotpoint || index.size() < 10) {
            lock.writeLock().lock();
            //二次判断，判断得到锁后是否符合条件
            long min = getMinHotPoint();
            if (min > hotpoint && index.size() >= 10) {
                lock.writeLock().unlock();
                return;
            }
            if (index.size() == 0) {
                index.put(pid, hotpoint);
                topPost.put(pid, fetcher.getInfo(pid));
                lock.writeLock().unlock();
                return;
            }
            /**
             * compute()无论在不在都更新，返回最新值
             * computeifabsent 仅在不在是时候创建，创建后返回创建时指定的值
             * computeifPresent 仅在在的时候更新，没有的话不创建，返回null，有的话返回最新的值
             */
            /**
             * 当前不存在时插入热帖
             */
            if (index.computeIfPresent(pid, (k, v) -> hotpoint) == null) {
                log.info("新晋热帖");

                //没超出
                if (index.size() < 10) {
                    index.putIfAbsent(pid, hotpoint);
                    topPost.putIfAbsent(pid, fetcher.getInfo(pid));
                    lock.writeLock().unlock();
                    return;
                }

                // FIXME: 19-5-21 线程不安全，淘汰时其他用户如果改变热度，则删除失败
                //超出了
                Iterator<Map.Entry<Integer, Long>> entries = index.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry<Integer, Long> entry = entries.next();
                    //随机末尾淘汰
                    if (entry.getValue() == min) {
                        //删除原有的
                        topPost.remove(entry.getKey());
                        index.remove(entry.getKey());
                        break;
                    }
                }
                index.putIfAbsent(pid, hotpoint);
                topPost.putIfAbsent(pid, fetcher.getInfo(pid));
            }
            lock.writeLock().unlock();
        }

    }

    /**
     * 加读锁获取热帖榜最小热度，低精确
     *
     * @return
     */
    private long getMinHotPointOfLock() {
        if (index.size() == 0) {
            return 0L;
        }
        lock.readLock().lock();
        Collection<Long> c = index.values();
        Object[] obj = c.toArray();
        Arrays.sort(obj);
        lock.readLock().unlock();
        return (long) obj[0];
    }

    /**
     * 获取热帖榜最小热度，高精确（需要在写锁中使用）
     *
     * @return
     */
    private long getMinHotPoint() {
        if (index.size() == 0) {
            return 0L;
        }
        Collection<Long> c = index.values();
        Object[] obj = c.toArray();
        Arrays.sort(obj);
        return (long) obj[0];
    }

    @Override
    public List<E> getTopPost() {
        List<E> list = new ArrayList<>(topPost.values());
        return list;
    }


    @Override
    public Map<Integer, Long> getTopPostHotPoint() {
        return index;
    }

    @Override
    public void addRefreshTask(int period) {
        addSaveTaskThread();
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = getTimeMillis(refreshTime) - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;

        scheduledExecutorService.scheduleAtFixedRate(new DefaultRefresh(), initDelay, period, TimeUnit.MILLISECONDS);
    }

    @Override
    public void addCycleSaveTaskForReload(int period, TimeUnit unit, String path) {
        if (path == null) {
            return;
        }
        this.path = path;
        scheduledExecutorService.scheduleAtFixedRate(new SaveForReloadTask(), 0, period, unit);
    }

    private void addSaveTask(Set<Integer> tasks) {
        tasks.forEach(k -> {
            taskQueue.add(new SaveEntity(k, false));
        });
    }

    @Override
    public void addCycleSaveTask(int period, TimeUnit unit) {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            log.info("开始添加保存任务，时间：{}", DateUtil.now());
            addSaveTask((hotPointCache.getMap()).keySet());
        }, period, period, unit);
    }

    private void addSaveTask(int pid) {
        taskQueue.offer(new SaveEntity(pid, true));
    }

    /**
     * 添加保存线程
     */
    private void addSaveTaskThread() {
        for (int i = 0; i < saveTaskThreadNum; i++) {
            consumerThread.execute(new SaveTask());
        }
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
        log.info("开始重置热帖榜：{}", DateUtil.now());
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

        index.clear();
        log.info("重置成功：{}", DateUtil.now());
        lock.writeLock().unlock();

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
                SaveEntity entity = taskQueue.poll();
                while (entity != null) {
                    //一旦出现任务，重置等待延时
                    sleepTime = SLEEP_STEP;
                    if (entity.isCheck() && hotPointCache.get(entity.getPid()).getViewIncrement() < updateThreshold) {
                        entity = taskQueue.poll();
                        continue;
                    }
                    dataSaver.save(entity.getPid(), hotPointCache.get(entity.getPid()));
                    entity = taskQueue.poll();
                }
                try {
                    Thread.sleep(sleepTime < MAX_SLEEP ? sleepTime += SLEEP_STEP : sleepTime);
                } catch (InterruptedException e) {
                    log.error("保存线程挂掉：{} 时间：{}", e.getMessage(), DateUtil.now());
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
            new LoadRecord<E>().saveRecord(viewCache, hotPointCache, index, topPost, path);
        }
    }
}
