package impl;

import interfaces.HotPointCache;
import interfaces.ViewCache;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class LoadRecord<E> {

    private ViewCache viewCache = null;

    private HotPointCache hotPointCache = null;

    private ConcurrentHashMap<Integer, Long> indexCache = null;
    private ConcurrentHashMap<Integer, E> hotPostCache = null;

    public ConcurrentHashMap<Integer, Long> getIndexCache() {
        return indexCache;
    }

    public ConcurrentHashMap<Integer, E> getHotPostCache() {
        return hotPostCache;
    }

    public ViewCache getViewCache() {
        return viewCache;
    }

    public HotPointCache getHotPointCache() {
        return hotPointCache;
    }


    public boolean reloadRecord(String path) {
        try (ObjectInputStream viewCacheInput =
                     new ObjectInputStream(new FileInputStream(path +
                             "viewCache"));
             ObjectInputStream hotPointInput = new ObjectInputStream(new FileInputStream(path +
                     "hotPointCache"));
             ObjectInputStream indexInput =
                     new ObjectInputStream(new FileInputStream(path + "indexCache"));
             ObjectInputStream hotPostInput =
                     new ObjectInputStream(new FileInputStream(path + "hotPostCache"))) {
            viewCache = (ViewCache) viewCacheInput.readObject();
            hotPointCache = (HotPointCache) hotPointInput.readObject();
            indexCache = (ConcurrentHashMap<Integer, Long>) indexInput.readObject();
            hotPostCache = (ConcurrentHashMap<Integer, E>) hotPostInput.readObject();
            log.info("重载缓存成功：");
            return true;
        } catch (IOException | ClassNotFoundException e) {
            log.error("重载失败：{} 堆栈信息:{}", e.getMessage(), e.getStackTrace());
            return false;
        }
    }

    void saveRecord(ViewCache viewCache, HotPointCache hotPointCache,
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
        } catch (IOException e) {
            log.error("保存失败：{}", e.getMessage());
        }
    }
}


