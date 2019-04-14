package impl;

import annotation.RefreshHotPost;
import interfaces.AbstractHotPostHandler;
import interfaces.Fetcher;
import interfaces.HotPointCache;
import interfaces.ViewCache;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class DefaultHotPostHandler<E> extends AbstractHotPostHandler<E> {

    public DefaultHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache, Fetcher fetcher) {
        super(viewCache, hotPointCache, fetcher);
    }

    public Object getID() {
        return fetcher.getUID(SecurityUtils.getSubject().getPrincipal());
    }

    protected void computeViewNum(int pid) {
        if (viewCache.putIfAbsent((int) getID(), pid) == null) {
            //需要计算浏览量
            /**
             * 默认热度
             */
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
        int type = -1;
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
            switch (type) {
                case 0:
                    computeViewNum(pid);
                    break;
                case 1:
                    addReplyNum(pid);
                    break;
                case 2:
                    del(pid);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 判断pid是否影响了热帖排行，并且删除对应缓存
     *
     * @param pid
     */
    protected void del(int pid) {
        topPost.remove(pid);
        index.remove(pid);


        if (dataSaver != null) {
            dataSaver.save(pid, hotPointCache.get(pid));
        }
        hotPointCache.remove(pid);
        viewCache.remove(pid);
    }

    protected void updateTopPost(int pid, long hotpoint) {
        //实时淘汰
        if (lowestHotpoint < hotpoint) {
            lock.readLock().lock();
            //二次判断，判断得到锁后是否符合条件
            if (lowestHotpoint > hotpoint) {
                lock.readLock().unlock();
                return;
            }
            if (index.size() == 0) {
                index.put(pid, hotpoint);
                topPost.put(pid, fetcher.getInfo(pid));
                lowestHotpoint = hotpoint;
                lock.readLock().unlock();
                return;
            }
            if (index.containsValue(pid)) {
                //如果存在则更新热度
                Iterator<Map.Entry<Integer, Long>> entries = index.entrySet().iterator();
                while (entries.hasNext()) {
                    {
                        Map.Entry<Integer, Long> entry = entries.next();
                        if (entry.getKey() == pid) {
                            //删除原有的
                            index.remove(entry.getKey());
                        }
                    }
                }
                index.put(pid, hotpoint);
                lowestHotpoint = hotpoint;
                lock.readLock().unlock();
                return;
            }
            //不存在，则直接删除最开始的那个；
            Iterator<Map.Entry<Integer, Long>> entries = index.entrySet().iterator();
            while (entries.hasNext()) {
                {
                    Map.Entry<Integer, Long> entry = entries.next();
                    if (entry.getValue() == lowestHotpoint) {
                        //删除原有的
                        index.remove(entry.getKey());
                        topPost.remove(entry.getKey());
                    }
                }
            }
            index.put(pid, hotpoint);
            topPost.put(pid, fetcher.getInfo(pid));
            lowestHotpoint = hotpoint;
            lock.readLock().unlock();
        }

    }

    public List<E> getTopPost() {
        List list = new ArrayList();
        topPost.forEachValue(1, v -> list.add(v));
        return list;
    }

    public void addTask(int period, TimeUnit unit) {
        if (hasTask == true) {
            return;
        }
        scheduledExecutorService.scheduleAtFixedRate(new DefaultRefresh(), 0, period, unit);
    }

    private class DefaultRefresh extends Refresh {
        @Override
        public void run() {
            /**
             * 清空顺序很关键
             * viewcache→hotpointcache 所以先清空hotpointcache不会引起数据的不一致性
             */
            if (dataSaver != null) {
                dataSaver.saveAll();
            } else {
                hotPointCache = hotPointCache.refresh();
            }
            viewCache = viewCache.refresh();
            /**
             * toppost不需要刷新，保留上一周的，仅把热度统一更新为固定值
             * 这里加锁对应 缓存更新到索引中，保证了一致性
             */
            lock.writeLock().lock();
            index.forEach((k, v) -> index.compute(k, (x, y) -> defaultHotpoint));
            lock.writeLock().unlock();
        }
    }

}
