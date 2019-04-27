package impl;

import interfaces.HotPointCache;
import interfaces.ViewCache;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LoadRecord {
    private static ViewCache viewCache = null;

    private static HotPointCache hotPointCache = null;

    private static ConcurrentHashMap indexCache = null;
    private static ConcurrentHashMap hotPostCache = null;

    public static ConcurrentHashMap getIndexCache() {
        return indexCache;
    }

    public static ConcurrentHashMap getHotPostCache() {
        return hotPostCache;
    }

    public static ViewCache getViewCache() {
        return viewCache;
    }

    public static HotPointCache getHotPointCache() {
        return hotPointCache;
    }

    public static boolean reloadRecord(String path) {
        try (ObjectInputStream view = new ObjectInputStream(new FileInputStream(path + "viewCache"));
             ObjectInputStream hotPoint = new ObjectInputStream(new FileInputStream(path + "hotPointCache"));
             ObjectInputStream index =
                     new ObjectInputStream(new FileInputStream(path + "indexCache"));
             ObjectInputStream hotPost =
                     new ObjectInputStream(new FileInputStream(path + "hotPostCache"));) {
            viewCache = (ViewCache) view.readObject();
            hotPointCache = ((HotPointCache) hotPoint.readObject());
            indexCache = (ConcurrentHashMap) index.readObject();
            hotPostCache = (ConcurrentHashMap) hotPost.readObject();
            log.info("重载缓存成功：");
            return true;
        } catch (IOException | ClassNotFoundException e) {
            log.error("重载失败：{}", e.getMessage());
            return false;
        }
    }

    static void saveRecord(ViewCache viewCache, HotPointCache hotPointCache,
                           ConcurrentHashMap indexCache, ConcurrentHashMap hotPostCache,
                           String path) {
        try (ObjectOutputStream view = new ObjectOutputStream(new FileOutputStream(path +
                "/viewCache"));
             ObjectOutputStream hotPoint = new ObjectOutputStream(new FileOutputStream(path +
                     "/hotPointCache"));
             ObjectOutputStream index = new ObjectOutputStream(new FileOutputStream(path +
                     "/indexCache"));
             ObjectOutputStream hotPost = new ObjectOutputStream(new FileOutputStream(path +
                     "/hotPostCache"))) {
            view.writeObject(viewCache);
            hotPoint.writeObject(hotPointCache);
            index.writeObject(indexCache);
            hotPost.writeObject(hotPostCache);
            view.flush();
            hotPoint.flush();
            index.flush();
            hotPost.flush();
        } catch (IOException e) {
            log.error("保存失败：{}", e.getMessage());
        }
    }
}
