package utils;

import caching.CacheConfig;
import caching.CacheConfigDeserializer;
import caching.Key;
import caching.KeyDeserializer;
import com.google.gson.*;
import spark.ResponseTransformer;

import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class JsonUtil {

    public static Gson customFromGson(Class c, JsonDeserializer jsonDeserializer){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(c, jsonDeserializer);
        return gsonBuilder.create();
    }
    public static TreeMap<String,String> fromTreeMap(String jsonKey){
        return new Gson().fromJson(jsonKey, TreeMap.class);
    }
    public static CacheConfig fromCacheConfig(String jsonCacheConfig){
        Gson gson = customFromGson(CacheConfig.class, new CacheConfigDeserializer());
        return gson.fromJson(jsonCacheConfig, CacheConfig.class);
    }
    public static Key fromKey(String jsonKey){
        Gson gson = customFromGson(Key.class, new KeyDeserializer());
        return gson.fromJson(jsonKey, Key.class);
    }

    public static String toJson(Object object){
        return new Gson().toJson(object);
    }
    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }
}
