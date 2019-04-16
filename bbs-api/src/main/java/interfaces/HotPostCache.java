package interfaces;

/**
 * 热帖的缓存
 * 热帖缓存包含 用户id+帖子id:1（检验浏览量） 帖子：热度（回复+浏览量）
 */
public interface HotPostCache<K, V> {
    V get(K k);

    Object putIfAbsent(K k, V v);

    HotPostCache refresh();

    void remove(K k);


}
