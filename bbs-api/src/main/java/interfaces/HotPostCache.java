package interfaces;

/**
 * 热帖的缓存
 * 热帖缓存包含 用户：帖子（检验浏览量） 帖子：热度（回复+浏览量）
 */
public interface HotPostCache<K, V> {
    V get(K k);

    void set(K k, V v);

    boolean exist(K k);

    HotPostCache refresh();
}
