package interfaces;

import java.util.Map;

public interface HotPointCache extends HotPostCache<Integer, HotPoint> {
    /**
     * 需要达成原子化更新
     *
     * @param key
     * @param hotPoint 增加的热度
     * @return
     */
    Long createOrUpdate(Integer key, Long hotPoint);

    Map<Integer, HotPoint> getMap();

    HotPointCache refresh();

    void delReply(int pid);

    //重置热度增量
    void reset(int pid);
}
