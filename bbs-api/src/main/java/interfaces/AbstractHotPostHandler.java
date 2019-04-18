package interfaces;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * fixme 暂时仅作为defaultHotPostHandler的抽象，需要优化
 *
 * @param <E> 帖子类型
 */
@Aspect
public abstract class AbstractHotPostHandler<E> {
    public static final int VIEW = 0;

    public static final int REPLY = 1;

    public static final int REFRESH = 2;

    /**
     * 刷新后默认热度为1000
     */
    protected long defaultHotpoint = 100;
    protected ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(2);

    protected AbstractDataSaver dataSaver = null;
    /**
     * 使用读写锁，加快非刷新时的计算速度
     */
    protected ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
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

    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 100,
            multiplier = 1.5))
    protected abstract void del(int pid);

    protected abstract void updateTopPost(int pid, long hotpoint);

    public abstract List<E> getTopPost();

    public abstract Map<Integer, Long> getTopPostHotPoint();

    //增加定期保存任务，
    public abstract void addCycleSaveTask(int period, TimeUnit unit) ;

    public abstract long getHotPoint(int pid);

}
