package sketching;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * Created by heka1203 on 2017-04-01.
 */
public interface Cache<E> {
    List<E> get(String key, String filter);
    E put(String key, int value);
    String getName();
    void setCacheConfig(CacheConfig cacheConfig);
    CacheConfig getCacheConfig();
}
