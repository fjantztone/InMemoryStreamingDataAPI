package sketching;

import com.google.gson.JsonObject;
import exceptions.RequiredDateException;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-01.
 */
public interface Cache<E> {
    List<E> get(TreeMap<String,String> key, String filter) throws RequiredDateException;
    E put(TreeMap<String,String> key, int value) throws RequiredDateException;
    String getName();
    void setCacheConfig(CacheConfig cacheConfig);
    CacheConfig getCacheConfig();
}
