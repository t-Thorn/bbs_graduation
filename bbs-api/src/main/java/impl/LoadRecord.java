package impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import interfaces.HotPointCache;
import interfaces.ViewCache;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
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
        try (JsonReader viewCacheFile = new JsonReader(new BufferedReader(new FileReader(path +
                "viewCache")));
             JsonReader hotPointFile = new JsonReader(new BufferedReader((new FileReader(path +
                     "hotPointCache"))));
             JsonReader indexFile =
                     new JsonReader(new BufferedReader(new FileReader(path + "indexCache")));
             JsonReader hotPostFile =
                     new JsonReader(new BufferedReader(new FileReader(path + "hotPostCache")))) {
            Gson gson = new Gson();
            Type type = new TypeToken<ConcurrentHashMap<Integer, E>>() {
            }.getType();
            viewCache = gson.fromJson(viewCacheFile, DefaultViewCache.class);
            hotPointCache = gson.fromJson(hotPointFile, DefaultHotPointCache.class);
            indexCache = gson.fromJson(indexFile,
                    new TypeToken<ConcurrentHashMap<Integer, Long>>() {
                    }.getType());
            hotPostCache = gson.fromJson(hotPostFile,
                    ConcurrentHashMap.class);
            log.info("重载缓存成功：");
            return true;
        } catch (IOException e) {
            log.error("重载失败：{} 堆栈信息:{}", e.getMessage(), e.getStackTrace());
            return false;
        }
    }

    void saveRecord(ViewCache viewCache, HotPointCache hotPointCache,
                    ConcurrentHashMap indexCache, ConcurrentHashMap hotPostCache,
                    String path) {
        try (JsonWriter view = new JsonWriter(new FileWriter(path +
                "/viewCache"));
             JsonWriter hotPoint = new JsonWriter(new FileWriter(path +
                     "/hotPointCache"));
             JsonWriter index = new JsonWriter(new FileWriter(path +
                     "/indexCache"));
             JsonWriter hotPost = new JsonWriter(new FileWriter(path +
                     "/hotPostCache"))) {
            Gson gson = new Gson();
            gson.toJson(viewCache, DefaultViewCache.class, view);
            gson.toJson(hotPointCache, DefaultHotPointCache.class, hotPoint);
            gson.toJson(indexCache, new TypeToken<ConcurrentHashMap<Integer, Long>>() {
                    }.getType(),
                    index);
            gson.toJson(hotPostCache, new TypeToken<ConcurrentHashMap<Integer, E>>() {
                    }.getType(),
                    hotPost);
        } catch (IOException e) {
            log.error("保存失败：{}", e.getMessage());
        }
    }
}


