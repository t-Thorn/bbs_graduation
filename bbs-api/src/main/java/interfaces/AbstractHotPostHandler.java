package interfaces;

import domain.HotPoint;
import impl.DefaultHotPointCache;
import impl.DefaultViewCache;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @param <E> 帖子类型
 */
@Aspect
public abstract class AbstractHotPostHandler<E> {

    /**
     * 定时任务线程池, 一个定时刷新，一个实时保存到本地，一个定时保存
     */
    protected ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(3);

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
    protected ConcurrentHashMap<Integer, E> topPost = new ConcurrentHashMap<>(10);
    /**
     * 索引
     * pid:hotpoint
     */
    protected ConcurrentHashMap<Integer, Long> index = new ConcurrentHashMap<>(10);

    /**
     * 初始化 热帖处理器 需要实现的部分：fetcher和dataSaver（可选，用于保存数据到数据库）
     *
     * @param viewCache     浏览记录缓存
     * @param hotPointCache 热度缓存
     * @param fetcher       信息提取器
     */
    public AbstractHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache,
                                  Fetcher<E> fetcher) {
        this.viewCache = viewCache == null ? new DefaultViewCache() : viewCache;
        this.hotPointCache = hotPointCache == null ? new DefaultHotPointCache() : hotPointCache;
        this.fetcher = fetcher;
    }

    public void setDataSaver(AbstractDataSaver dataSaver) {
        this.dataSaver = dataSaver;
    }

    @Order(2)
    @AfterReturning("aopIntercept()")
    protected abstract void process(JoinPoint point);

    /**
     * 计算浏览量
     *
     * @param pid
     */
    protected abstract void computeViewNum(int pid);

    protected abstract void addReplyNum(int pid);

    @Pointcut("@annotation(annotation.RefreshHotPost)")
    private void aopIntercept() {

    }

    protected abstract void del(int pid);

    protected abstract void updateTopPost(int pid, long hotpoint);

    public abstract List<E> getTopPost();

    public abstract Map<Integer, Long> getTopPostHotPoint();

    /**
     * 增加刷新热度任务
     */
    public abstract void addRefreshTask(int period);

    /**
     * 获取帖子热度
     *
     * @param pid
     * @return
     */
    public abstract HotPoint getHotPoint(int pid);

    /**
     * 循环保存缓存
     *
     * @param period
     * @param unit
     * @param path   保存路径
     */
    public abstract void addCycleSaveTaskForReload(int period, TimeUnit unit, String path);

    /**
     * 增加循环保存任务
     */
    public abstract void addCycleSaveTask(int period, TimeUnit unit);

}
