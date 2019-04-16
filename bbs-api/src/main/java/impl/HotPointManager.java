package impl;

import interfaces.AbstractHotPostHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class HotPointManager<E> {
    private AbstractHotPostHandler handler;

    public HotPointManager(AbstractHotPostHandler handler) {
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

    public long getHotPoint(int pid) {
        return handler.getHotPoint(pid);
    }
}
