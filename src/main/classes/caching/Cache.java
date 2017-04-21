package caching;

import exceptions.InvalidKeyException;
import models.CacheConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-01.
 */
public interface Cache<E> {
    E pointGet(TreeMap<String,String> key, LocalDateTime localDateTime);
    List<E> pointsGet(TreeMap<String,String> key, LocalDateTime startDateTime, LocalDateTime endDateTime);
    E rangeGet(TreeMap<String,String> key, LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<E> topGet(int days);
    List<E> put(TreeMap<String,String> key, LocalDateTime localDateTime, int amount) throws InvalidKeyException;

    boolean hasExpired(LocalDateTime now);
    void setCacheConfig(CacheConfig cacheConfig);
    CacheConfig getCacheConfig();
}
