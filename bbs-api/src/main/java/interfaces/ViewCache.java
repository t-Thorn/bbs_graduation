package interfaces;

public interface ViewCache extends HotPostCache<Integer, Object> {
    ViewCache refresh();
}
