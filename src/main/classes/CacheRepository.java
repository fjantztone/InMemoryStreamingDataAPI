import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import exceptions.CacheNotFoundException;
import exceptions.RequiredDateException;
import org.bson.Document;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.InputField;
import sketching.NamedCache;
import static utils.JsonUtil.*;
import utils.ParseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
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
        return createCache(_cacheConfig);
    }
    public Cache createCache(CacheConfig cacheConfig){
        return new NamedCache(cacheConfig);
    }
    public CacheConfig createCacheConfig(String cacheConfig){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
        Gson gson = gsonBuilder.create();
        CacheConfig _cacheConfig = gson.fromJson(cacheConfig, CacheConfig.class);
        return _cacheConfig;
    }
    public CacheConfig editCache(String cacheConfig) throws CacheNotFoundException {
        CacheConfig _cacheConfig = createCacheConfig(cacheConfig);
        Cache cache = getCache(_cacheConfig.getCacheName());
        //TODO: write update to DB
        cache.setCacheConfig(_cacheConfig);
        return _cacheConfig;

    }
    public CacheConfig addCache(Cache cache){
        Document document = Document.parse(toJson(cache.getCacheConfig()));
        database.getCollection("cacheConfigs").insertOne(document);
        //check if cache already exists!
        caches.add(cache);
        return cache.getCacheConfig();
    }
    public Cache getCache(String cacheName) throws CacheNotFoundException {
        for (Cache cache : caches) {
            if (cache.getName().equalsIgnoreCase(cacheName)) {
                return cache;
            }
        }
        throw new CacheNotFoundException(String.format("Cache with %s does not exist.", cacheName));
    }

    //TODO: return key
    public Object addCacheKey(TreeMap<String,String> key, Cache cache){
        Document _key = Document.parse(toJson(key));
        _key.append("cacheName", cache.getName());
        database.getCollection("cacheKeys").insertOne(_key);
        return cache.put(key, 1);
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
                cache.put(toSortedMap(key), 1);
            }
            //Do not add to db
            caches.add(cache);
        }
    }
}
