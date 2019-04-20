package interfaces;

import java.util.Map;

public interface ViewCache extends HotPostCache<String, Integer> {
    ViewCache refresh();

    void removeLike(int pid);

    Map<String, Integer> getMap();
}
