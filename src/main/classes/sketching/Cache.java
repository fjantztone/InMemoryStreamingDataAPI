package sketching;

import com.google.gson.JsonObject;

/**
 * Created by heka1203 on 2017-04-01.
 */
public interface Cache<E> {
    E topQuery();
    E pointQuery(Object key);
    E rangeQuery(Object key);
    boolean put(Object key, int value);
    boolean put(Object key);
    String getName();
    void setCacheConfig(CacheConfig cacheConfig);
    CacheConfig getCacheConfig();
}
