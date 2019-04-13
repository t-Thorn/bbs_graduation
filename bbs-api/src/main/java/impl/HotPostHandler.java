package impl;

import annotation.RefreshHotPost;
import interfaces.Fetcher;
import interfaces.HotPointCache;
import interfaces.ViewCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Aspect
public class HotPostHandler<E> {
    public static final int VIEW = 0;

    public static final int REPLY = 1;
    private static ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(1);
    private ReentrantLock lock = new ReentrantLock();
    private boolean hasTask = false;
    private ViewCache viewCache;
    private HotPointCache hotPointCache;
    private Fetcher<E> fetcher;
    /**
     * pid:post
     */
    private ConcurrentHashMap<Integer, E> topPost = new ConcurrentHashMap<>();

    /**
     * hotpoint:pid
     */
    private ConcurrentSkipListMap<Integer, Integer> index = new ConcurrentSkipListMap<>();

    public HotPostHandler(ViewCache viewCache, HotPointCache hotPointCache, Fetcher fetcher) {
        this.viewCache = viewCache;
        this.hotPointCache = hotPointCache;
        this.fetcher = fetcher;
    }

    public static HotPostHandler getSimpleHandler(Fetcher fetcher) {
        return new HotPostHandler(new DefaultViewCache(), new DefaultHotPointCache(), fetcher);
    }

    private void computeViewNum(int pid) {

    }

    private void addReplyNum(int pid) {

    }

    @Pointcut("@annotation(annotation.RefreshHotPost)")
    private void aopIntercept() {

    }

    @AfterReturning("aopIntercept()")
    private void process(JoinPoint point) {
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
                    refresh(pid);
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
    private void refresh(int pid) {

    }

    private void updateTopPost(int pid, int hotpoint) {
        //实时淘汰
        if (index.firstKey() < hotpoint) {
            lock.lock();
            //二次判断，判断得到锁后是否符合条件
            if (index.firstKey() > hotpoint) {
                lock.unlock();
                return;
            }
            if (index.size() == 0) {
                index.put(hotpoint, pid);
                topPost.put(pid, fetcher.getInfo(pid));
                lock.unlock();
                return;
            }
            if (index.containsValue(pid)) {
                //如果存在则更新热度
                Iterator<Map.Entry<Integer, Integer>> entries = index.entrySet().iterator();
                while (entries.hasNext()) {
                    {
                        Map.Entry<Integer, Integer> entry = entries.next();
                        if (entry.getValue() == pid) {
                            //删除原有的
                            index.remove(entry.getKey());
                        }
                    }
                }
                index.put(hotpoint, pid);
                lock.unlock();
                return;
            }
            //不存在，则直接删除最开始的那个；
            topPost.remove(index.get(index.firstKey()));
            index.remove(index.firstKey());
            index.put(hotpoint, pid);
            topPost.put(pid, fetcher.getInfo(pid));
            lock.unlock();
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
        scheduledExecutorService.scheduleAtFixedRate(new Refresh(), 0, period, unit);
    }

    private class Refresh implements Runnable {
        @Override
        public void run() {
            lock.lock();
            topPost.clear();
            viewCache.refresh();
            hotPointCache.refresh();
            lock.unlock();
        }
    }
}
