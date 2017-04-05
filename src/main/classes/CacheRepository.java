import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.CacheField;
import sketching.NamedCache;
import utils.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-05.
 */
public class CacheRepository {
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    MongoDatabase database = mongoClient.getDatabase("streamingdata");
    Logger logger = Logger.getLogger(CacheRepository.class.getName());

    public List<Cache> caches = new ArrayList<>();

    public CacheRepository(){
        initialize();
    }

    public Cache createCache(String cacheConfig){
        CacheConfig _cacheConfig = createCacheConfig(cacheConfig);
        //^ handle parse exception
        NamedCache cache = new NamedCache(_cacheConfig);
        return cache;
    }
    public CacheConfig createCacheConfig(String cacheConfig){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
        Gson gson = gsonBuilder.create();
        CacheConfig _cacheConfig = gson.fromJson(cacheConfig, CacheConfig.class);
        return _cacheConfig;
    }
    public CacheConfig editCache(String cacheConfig){
        CacheConfig _cacheConfig = createCacheConfig(cacheConfig);
        Cache cache = getCache(_cacheConfig.getCacheName());
        if(cache != null){
            cache.setCacheConfig(_cacheConfig);
            return _cacheConfig;
        }
        return null;
    }
    public CacheConfig addCache(Cache cache){
        Document document = Document.parse(JsonUtil.toJson(cache.getCacheConfig()));
        database.getCollection("cacheConfigs").insertOne(document);
        //check if cache already exists!
        caches.add(cache);
        return cache.getCacheConfig();
    }
    protected Cache getCache(String cacheName){
        for (Cache cache : caches) {
            if (cache.getName().equalsIgnoreCase(cacheName)) {
                return cache;
            }
        }
        return null;
    }
    public List<CacheField> getAllowableCacheFields(String cacheName){
        Cache cache = getCache(cacheName);
        if(cache != null){
            return cache.getCacheConfig().getCacheFields();
        }
        return null;
    }
    //TODO: return key
    public Object addCacheKey(String key, String cacheName){
        Cache cache = getCache(cacheName);
        if(cache != null){
            Document _key = Document.parse(key);
            _key.append("cacheName", cacheName);
            database.getCollection("cacheKeys").insertOne(_key);
            return cache.put(key, 1);
        }
        System.out.println("Cache not found..");
        return null;

    }
    public Object getCacheEntry(String key, String cacheName, String filter){
        Cache cache = getCache(cacheName);
        if(cache != null){
            return cache.get(key, filter);
        }
        return null;
    }
    protected void initialize(){
        //TODO: Fix smarter storage to avoid projections.
        FindIterable<Document> cacheConfigs = database.getCollection("cacheConfigs").find().projection(Document.parse("{'_id' : 0}"));
        for(Document cc : cacheConfigs){
            String cacheConfig = cc.toJson();
            Cache cache = createCache(cacheConfig);
            System.out.println("Loaded new cache: " + cache.getCacheConfig());
            FindIterable<Document> keys = database.getCollection("cacheKeys").find(Document.parse("{'cacheName' : '"+cache.getName()+"'}")).projection(Document.parse("{'_id' : 0, 'cacheName' : 0}"));
            for(Document key : keys){
                //Do not add to db
                cache.put(key.toJson(), 1);
                System.out.println(key.toJson());
            }
            //Do not add to db
            caches.add(cache);
        }
    }

}
