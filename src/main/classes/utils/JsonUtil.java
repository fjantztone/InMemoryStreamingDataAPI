package utils;

import caching.CacheConfig;
import caching.CacheConfigDeserializer;
import caching.Key;
import caching.KeyDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParser;
import spark.ResponseTransformer;

import java.lang.reflect.Type;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class JsonUtil {

    public static Gson customGson(Class c, JsonDeserializer jsonDeserializer){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(c, jsonDeserializer);
        return gsonBuilder.create();
    }

    public static TreeMap<String,String> fromTreeMap(String jsonKey){
        return new Gson().fromJson(jsonKey, TreeMap.class);
    }
    public static CacheConfig fromCacheConfig(String jsonCacheConfig){
        Gson gson = customGson(CacheConfig.class, new CacheConfigDeserializer());
        return gson.fromJson(jsonCacheConfig, CacheConfig.class);
    }
    public static Key fromKey(String jsonKey){
        Gson gson = customGson(Key.class, new KeyDeserializer());
        return gson.fromJson(jsonKey, Key.class);
    }

    public static String toJson(Object object){
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return json;
    }
    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }
}
