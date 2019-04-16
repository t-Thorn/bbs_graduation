package interfaces;

public interface ViewCache extends HotPostCache<String, Integer> {
    ViewCache refresh();

    void removeLike(int pid);
}
