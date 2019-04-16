package impl;

import annotation.RefreshHotPost;
import interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Slf4j
public class DefaultHotPostHandler<E> extends AbstractHotPostHandler<E> {
    @Override
    public long getHotPoint(int pid) {
        HotPoint hotPoint = hotPointCache.get(pid);
        return hotPoint == null ? 0 : hotPoint.getTotal();
    }

    public DefaultHotPostHandler(ViewCache viewCache, HotPointCache hotPointCache, Fetcher fetcher) {
        super(viewCache, hotPointCache, fetcher);
    }

    public Object getID(int pid) {
        Object user = SecurityUtils.getSubject().getPrincipal();
        if (user != null) {
            return fetcher.getUID(user) + ":" + pid;
        }
        //利用ip地址的负数作为id
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes sra = ((ServletRequestAttributes) ra);
        HttpServletRequest request = sra.getRequest();
        return Math.negateExact(Integer.valueOf(getIpAddress((request)).replace(".",
                "").replace(":", ""))) + ":" + pid;
    }


    /**
     * 获取真实ip，可以避免代理
     *
     * @param request
     * @return
     */
    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    protected void computeViewNum(int pid, Object object) {
        if (viewCache.putIfAbsent((String) getID(pid), 1) == null) {
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
            log.info("准备计算热度：type：{} pid:{}", type, pid);
            switch (type) {
                case 0:
                    computeViewNum(pid, null);
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
    protected synchronized void del(int pid) {
        topPost.remove(pid);
        index.remove(pid);
        //重置门槛，仅当热帖量超出10时再计算
        lowestHotpoint = 0;
        //筛选当前热度第十名：性能消耗可能更大，因为遍历的是当时全部的帖子集合

        if (dataSaver != null) {
            dataSaver.save(pid, hotPointCache.get(pid));
        }
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

    public void addReFreshTask(int period, TimeUnit unit) {
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
            //重置门槛，仅当热帖量超出10时再计算
            lowestHotpoint = 0;
            lock.writeLock().unlock();
        }
    }


}
