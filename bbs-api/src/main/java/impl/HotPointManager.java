package impl;

import domain.HotPoint;
import interfaces.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HotPointManager<E> {
    public static final String VIEW = "view";

    public static final String REPLY = "reply";

    public static final String DELREPLY = "delReply";

    public static final String DELPOST = "delPost";

    private AbstractHotPostHandler<E> handler;

    public HotPointManager(ViewCache viewCache, HotPointCache hotPointCache, Fetcher fetcher) {
        this.handler = new DefaultHotPostHandler<E>(viewCache, hotPointCache, fetcher);
    }

    public HotPointManager(AbstractHotPostHandler<E> handler) {
        this.handler = handler;
    }

    public List<E> getTopPost() {
        if (handler == null) {
            log.error("handler为空");
            throw new NullPointerException();
        }
        return handler.getTopPost();
    }

    public Map<Integer, Long> getHotPoint() {
        if (handler == null) {
            log.error("handler为空");
            throw new NullPointerException();
        }
        return handler.getTopPostHotPoint();
    }

    public HotPoint getHotPoint(int pid) {
        return handler.getHotPoint(pid);
    }

    public void addRefreshTask(int period, TimeUnit unit) {
        handler.addRefreshTask(period, unit);
    }

    public void addCycleSaveForReloadTask(int period, TimeUnit unit, String path) {
        handler.addCycleSaveTaskForReload(period, unit, path);
    }

    public void addDataSavor(AbstractDataSaver dataSaver) {
        handler.setDataSaver(dataSaver);
    }

    public void addCycleSaveThread(int period, TimeUnit unit) {
        handler.addCycleSaveTask(period, unit);
    }
}
