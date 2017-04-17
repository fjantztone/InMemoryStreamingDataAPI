package caching;

import exceptions.InvalidKeyException;
import exceptions.TopListNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-01.
 */
public interface Cache<E> {
    E pointGet(TreeMap<String,String> key, LocalDate localDate);
    List<E> pointsGet(TreeMap<String,String> key, LocalDate startDate, LocalDate endDate);
    List<E> rangeGet(TreeMap<String,String> key, LocalDate startDate, LocalDate endDate);
    List<E> topGet(int days);
    List<E> put(TreeMap<String,String> key, LocalDate localDate, int amount) throws InvalidKeyException;

    void setCacheConfig(CacheConfig cacheConfig);
    CacheConfig getCacheConfig();
}
