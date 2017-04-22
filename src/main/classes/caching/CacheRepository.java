package caching;

import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by heka1203 on 2017-04-05.
 */

public class CacheRepository {

    private final Map<String, Cache> caches;

    public CacheRepository(Map<String,Cache> caches){
        this.caches = new ConcurrentHashMap<>(caches);
    }

    public Cache addCache(Cache cache) throws CacheAlreadyExistsException {
        String cacheName = cache.getCacheConfig().getName();
        validateDuplicate(cacheName);
        return caches.put(cacheName, cache);
    }

    public Cache deleteCache(String cacheName) throws CacheNotFoundException {
        validatePresence(cacheName);
        return caches.remove(cacheName);
    }

    public Cache getCache(String cacheName) throws CacheNotFoundException {
        validatePresence(cacheName);
        return caches.get(cacheName);
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
