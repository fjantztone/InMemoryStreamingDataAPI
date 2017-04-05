import com.google.gson.*;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.NamedCache;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import utils.JsonUtil;
import utils.ParseUtil;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */


public class CacheService {
    //Should be read from file later on.
    public static List<Cache> caches = new ArrayList<>();
    public static Logger logger = Logger.getLogger(CacheService.class.getName());

    public static CacheConfig createCache(Request req, Response res) {

        try {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(req.body());
            JsonObject root = jsonElement.getAsJsonObject();
            //write to db

            JsonElement json = root.get("options");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
            Gson gson = gsonBuilder.create();

            CacheConfig cc = gson.fromJson(json, CacheConfig.class);
            NamedCache cache = new NamedCache(cc);
            caches.add(cache);
            return cc;

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e.getCause());
            throw e;
        }

    }
    public static CacheConfig editCache(Request req, Response res) {

        try {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(req.body());

            JsonObject root = jsonElement.getAsJsonObject();
            JsonElement json = root.get("options");


            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
            Gson gson = gsonBuilder.create();
            CacheConfig cc = gson.fromJson(json, CacheConfig.class);
            String cacheName = cc.getCacheName();

            Cache cache = getCache(cacheName);
            cache.setCacheConfig(cc);
            return cc;

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e.getCause());
            throw e;
        }

    }

    private static Cache getCache(String cacheName) {
        for (Cache cache : caches) {
            if (cache.getName().equalsIgnoreCase(cacheName)) {
                return cache;
            }
        }
        throw new IllegalArgumentException(String.format("Cache with name %s does not exist.", cacheName));
    }
    //writeThrough key in cache?
    public static Object putKey(Request req, Response res) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(req.body());

        JsonObject key = jsonElement.getAsJsonObject();
        Cache cache = getCache(req.params(":name"));
        return writeThrough(key, cache);
    }
    //upload keys to cache?
    public static Object uploadKeys(Request req, Response res) throws IOException, ServletException {
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/var/temp"));
        //use File.read?
        try (InputStream is = req.raw().getPart("uploaded_file").getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            Cache c = getCache(req.params(":name"));
            br.lines().forEach(key -> {writeThrough(key, c);});
        }
        return new ResponseText("Successfully updated valid keys in file to cache.");
    }
    //get key in cache?

    public static Object getKeyInCache(Request req, Response res){
        String cacheName = req.params(":name");
        String filter = req.params(":filter");
        JsonObject key = new JsonParser().parse(JsonUtil.toJson(req.queryMap().toMap()).replaceAll("\\[|\\]", "")).getAsJsonObject();
        System.out.println(key);
        Cache cache = getCache(cacheName);
        return cache.get(key, filter);
    }


    protected static Object writeThrough(Object key, Cache cache) {

        JsonObject parsedKey = ParseUtil.parse(key, cache.getCacheConfig().getCacheFields());
        if(parsedKey != null){
            cache.put(parsedKey, 1);
            //{cacheName : cacheName, key: parsedKey}

            //write to cacheName.data[]
        }

        return null;
    }
    public static void initialize(){
        //for each cacheconfig
            //create new cache
            //get all records for that cache
            //insert into new cache
    }
}
