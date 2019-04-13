package interfaces;

public interface ViewCache extends HotPostCache<Integer, Integer> {
    void mark(Integer uid, Integer pid);
}
