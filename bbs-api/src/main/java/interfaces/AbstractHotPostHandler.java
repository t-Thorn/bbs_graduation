package interfaces;

import impl.DefaultHotPointCache;
import impl.DefaultHotPostHandler;
import impl.DefaultViewCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Aspect
public abstract class AbstractHotPostHandler<E> implements ViewCounter {
    public static final int VIEW = 0;

    public static final int REPLY = 1;

    public static final int REFRESH = 2;

    /**
     * 刷新后默认热度为1000
     */
    protected long defaultHotpoint = 100;
    protected ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(1);
    protected AbstractDataSaver dataSaver = null;
    /**
     * 使用读写锁，加快非刷新时的计算速度
     */
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    protected boolean hasTask = false;
    protected ViewCache viewCache;
    protected HotPointCache hotPointCache;
    protected Fetcher<E> fetcher;
    /**
     * pid:post
     */
    protected ConcurrentHashMap<Integer, E> topPost = new ConcurrentHashMap<>();
    /**
     * 索引
     * pid:hotpoint
     */
    protected ConcurrentHashMap<Integer, Long> index = new ConcurrentHashMap<>();
    protected volatile long lowestHotpoint = 0l;

    /**
     * 初始化 热帖处理器 需要实现的部分：fetcher和dataSaver（可选，用于保存数据到数据库）
     *
     * @param viewCache     浏览记录缓存
     * @param hotPointCache 热度缓存
     * @param fetcher       信息提取器
     */
    public AbstractHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache, Fetcher fetcher) {
        this.viewCache = viewCache;
        this.hotPointCache = hotPointCache;
        this.fetcher = fetcher;
    }

    public static DefaultHotPostHandler getSimpleHandler(Fetcher fetcher) {
        return new DefaultHotPostHandler(new DefaultViewCache(), new DefaultHotPointCache(), fetcher);
    }

    public void setDefaultHotpoint(int defaultHotpoint) {
        defaultHotpoint = defaultHotpoint;
    }

    public void setDataSaver(AbstractDataSaver dataSaver) {
        this.dataSaver = dataSaver;
    }

    @Pointcut("@annotation(annotation.RefreshHotPost)")
    private void aopIntercept() {

    }

    @AfterReturning("aopIntercept()")
    protected abstract void process(JoinPoint point);

    /**
     * 计算浏览量
     *
     * @param pid
     * @param object 保留参数，默认实现是request请求来获取IP地址
     */
    protected abstract void computeViewNum(int pid, Object object);

    protected abstract void addReplyNum(int pid);

    protected abstract void del(int pid);

    protected abstract void updateTopPost(int pid, long hotpoint);

    public abstract List<E> getTopPost();

    public abstract Map<Integer, Long> getTopPostHotPoint();

    public abstract void addReFreshTask(int period, TimeUnit unit);

    /*//todo 添加定时保存任务 但不刷新缓存，可能需要重新设定结构（增加增量）
    public abstract void addSaveTask(int period, TimeUnit unit);*/

    public abstract long getHotPoint(int pid);

    protected abstract class Refresh implements Runnable {

    }
}
