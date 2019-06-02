package interfaces;

import java.util.Map;

public interface ViewCache extends HotPostCache<String, Integer> {
    @Override
    ViewCache refresh();

    void removeLike(int pid);

    Map<String, Integer> getMap();
}
