package caching;

import exceptions.TopListNotFoundException;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-01.
 */
public interface Cache<E> {
    List<E> get(TreeMap<String,String> key, String filter) throws TopListNotFoundException;
    List<E> put(TreeMap<String,String> key, int value);
    String getName();
    void setCacheConfig(CacheConfig cacheConfig);
    CacheConfig getCacheConfig();
}
