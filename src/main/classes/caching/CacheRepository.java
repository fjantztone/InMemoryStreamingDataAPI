package caching;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.InvalidKeyException;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by heka1203 on 2017-04-05.
 */

//TODO: Very confusing return types, error handling etc
public class CacheRepository {

    public final static Map<String, Cache> caches = new ConcurrentHashMap<>();

    public CacheRepository() throws CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException {
        initialize();
    }
    protected CacheConfig toCacheConfig(String cacheConfig){

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
        Gson gson = gsonBuilder.create();
        CacheConfig _cacheConfig = gson.fromJson(cacheConfig, CacheConfig.class);
        return _cacheConfig;
    }
    protected Key toKey(String key){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Key.class, new KeyDeserializer());
        Gson gson = gsonBuilder.create();
        Key _key = gson.fromJson(key, Key.class);
        return _key;
    }
    public Cache editCache(String cacheConfig) throws CacheNotFoundException { //not thread safe
        CacheConfig _cacheConfig = toCacheConfig(cacheConfig);
        Cache cache = getCache(_cacheConfig.getName());
        cache.setCacheConfig(_cacheConfig);
        return cache;
    }
    public Cache createCache(String cacheConfig) throws CacheAlreadyExistsException, CacheNotFoundException {
        CacheConfig _cacheConfig = toCacheConfig(cacheConfig);
        String cacheName = _cacheConfig.getName();
        validateDuplicate(cacheName);
        Cache cache = new NamedCache(_cacheConfig);
        caches.put(cacheName, cache);
        return cache;
    }
    public Cache deleteCache(String cacheName) throws CacheNotFoundException { //not thread safe
        validatePresence(cacheName);
        Cache removed = caches.remove(cacheName);
        return removed;
    }

    public Cache getCache(String cacheName) throws CacheNotFoundException {
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
    protected void initialize() throws CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException {

        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("streamingdata");
        FindIterable<Document> cacheConfigs = database.getCollection("cacheconfigs").find().projection(Document.parse("{'_id' : 0}"));

        for(Document cc : cacheConfigs){
            //convert and insert keys
            String cacheConfig = cc.toJson();
            Cache cache = createCache(cacheConfig);
            @SuppressWarnings("unchecked")
            List<Document> docs = (List<Document>)cc.get("data");
            for(Document doc : docs){
                Key key = toKey(doc.toJson());
                cache.put(key.getKey(), key.getCreatedAt(), 1);
                System.out.println(key.getCreatedAt());
            }
            System.out.printf("Loaded %d keys into cache.\n", docs.size());
        }
    }
}
