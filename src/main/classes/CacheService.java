import com.google.gson.*;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.NamedCache;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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
            String cacheName = req.params(":name");
            JsonObject root = jsonElement.getAsJsonObject();
            JsonElement json = root.get("options");
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
            Gson gson = gsonBuilder.create();

            CacheConfig cc = gson.fromJson(json, CacheConfig.class);
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
        throw new IllegalArgumentException(String.format("Cachename: %s does not exist.", cacheName));
    }

    public static ResponseText createTupleInCache(Request req, Response res) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(req.body());
        String cacheName = req.params(":name");

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        Cache cache = getCache(cacheName);

        boolean putted = cache.put(jsonObject, 1);
        if(putted)
            return new ResponseText("Successfully inserted tuple.");
        else
            throw new IllegalArgumentException("Tuple could not be inserted into cache. Make sure the JSON is correctly formatted.");
    }

    public static ResponseText uploadTupleToCache(Request req, Response res) throws IOException, ServletException {
        String cacheName = req.params(":name");
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/var/temp"));
        try (InputStream is = req.raw().getPart("uploaded_file").getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            Cache c = getCache(cacheName);
            //Ignore if some entries were not correctly parsed
            br.lines().forEach(c::put);
            return new ResponseText("Successfully read file to cache.");
        }
    }

    public static Object getTupleInCache(Request req, Response res){
        throw new UnsupportedOperationException();
    }


}
