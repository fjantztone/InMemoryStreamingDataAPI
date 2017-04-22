package caching;

import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by heka1203 on 2017-04-05.
 */

public class CacheRepository {


    private final Map<String, Cache> caches;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public CacheRepository(Map<String,Cache> caches){
        this.caches = new HashMap<>(caches);
    }

    public Cache put(String cacheName, Cache cache) throws CacheAlreadyExistsException {
        w.lock();
        validateDuplicate(cacheName);
        try{
            return caches.put(cacheName, cache);
        } finally {
            w.unlock();
        }
    }
    public Cache get(String cacheName) throws CacheNotFoundException {
        w.lock();
        validatePresence(cacheName);
        try{
            return caches.get(cacheName);
        } finally {
            w.unlock();
        }
    }
    public Cache delete(String cacheName) throws CacheNotFoundException {
        w.lock();
        validatePresence(cacheName);
        try{
            return caches.remove(cacheName);
        } finally {
            w.unlock();
        }
    }

    public void validatePresence(String cacheName) throws CacheNotFoundException {
        if(!caches.containsKey(cacheName))
            throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));
    }
    public void validateDuplicate(String cacheName) throws CacheAlreadyExistsException {
        if(caches.containsKey(cacheName))
            throw new CacheAlreadyExistsException(String.format("A cache with name: %s already exists. Try another cache name.", cacheName));
    }

}
