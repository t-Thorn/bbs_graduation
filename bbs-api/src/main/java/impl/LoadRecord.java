package impl;

import interfaces.HotPointCache;
import interfaces.ViewCache;

import java.io.*;

public class LoadRecord {
    private static ViewCache viewCache = null;

    private static HotPointCache hotPointCache = null;

    public static ViewCache getViewCache() {
        return viewCache;
    }

    public static HotPointCache getHotPointCache() {
        return hotPointCache;
    }

    public static boolean reloadRecord(String viewCacheFile, String hotPointCacheFile) {
        try (ObjectInputStream view = new ObjectInputStream(new FileInputStream(viewCacheFile));
             ObjectInputStream hotPoint = new ObjectInputStream(new FileInputStream(hotPointCacheFile));) {
            viewCache = (ViewCache) view.readObject();
            hotPointCache = ((HotPointCache) hotPoint.readObject());
            return true;
        } catch (IOException | ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean reloadRecord(String path) {
        return reloadRecord(path + "/viewCache", path +
                "/hotPointCache");
    }

    public static boolean saveRecord(ViewCache viewCache, HotPointCache hotPointCache, String path) {
        try (ObjectOutputStream view = new ObjectOutputStream(new FileOutputStream(path +
                "/viewCache"));
             ObjectOutputStream hotPoint = new ObjectOutputStream(new FileOutputStream(path +
                     "/hotPointCache"))) {
            view.writeObject(viewCache);
            hotPoint.writeObject(hotPointCache);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
