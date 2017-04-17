package caching;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.RequiresValidDateException;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-05.
 */

//TODO: Very confusing return types, error handling etc
public class CacheRepository {

    public Map<String, Cache> caches = new ConcurrentHashMap<>();

    public CacheRepository() throws CacheAlreadyExistsException, CacheNotFoundException {
        initialize();
    }
    protected CacheConfig toCacheConfig(String cacheConfig){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
        Gson gson = gsonBuilder.create();
        CacheConfig _cacheConfig = gson.fromJson(cacheConfig, CacheConfig.class);
        return _cacheConfig;
    }
    public synchronized Cache editCache(String cacheConfig) throws CacheNotFoundException {
        CacheConfig _cacheConfig = toCacheConfig(cacheConfig);
        Cache cache = getCache(_cacheConfig.getName());
        cache.setCacheConfig(_cacheConfig);
        return cache;
    }
    public synchronized Cache createCache(String cacheConfig) throws CacheAlreadyExistsException, CacheNotFoundException {
        CacheConfig _cacheConfig = toCacheConfig(cacheConfig);
        String cacheName = _cacheConfig.getName();
        validateDuplicate(cacheName);
        Cache cache = new NamedCache(_cacheConfig);
        caches.put(cacheName, cache);
        return cache;
    }
    public synchronized Cache deleteCache(String cacheName) throws CacheNotFoundException {
        validatePresence(cacheName);
        Cache removed = caches.remove(cacheName);
        return removed;
    }

    public synchronized Cache getCache(String cacheName) throws CacheNotFoundException {
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
    protected void initialize() throws CacheAlreadyExistsException, CacheNotFoundException {

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("streamingdata");
        FindIterable<Document> cacheConfigs = database.getCollection("cacheconfigs").find().projection(Document.parse("{'_id' : 0}"));

        for(Document cc : cacheConfigs){
            //convert and insert keys
            String cacheConfig = cc.toJson();
            Cache cache = createCache(cacheConfig);
            System.out.println("Loaded new cache: " + cache.getCacheConfig());
            /*FindIterable<Document> keys = database.getCollection("cacheKeys").find(Document.parse("{'cacheName' : '"+cache.getName()+"'}")).projection(Document.parse("{'_id' : 0, 'cacheName' : 0}")).sort(Document.parse("{'DATE' : 1}"));
            //keys.map(JsonUtil::toSortedMap).forEach((Block<? super TreeMap>) key -> {cache.put(key, 1);});
            long start = System.currentTimeMillis();
            for(Document key : keys){
                //Do not add to db

                cache.put(toSortedMap(key), 1);
            }
            long end = System.currentTimeMillis();
            System.out.printf("Time to load cache was: %ds\n", (end - start) / 1000L);*/
            //Do not add to db
        }
    }
}
