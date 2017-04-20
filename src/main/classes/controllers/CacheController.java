package controllers;

import caching.*;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import exceptions.*;
import org.bson.Document;
import org.slf4j.LoggerFactory;

import static utils.JsonUtil.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import static utils.DateUtil.validateISODate;

/**
 * Created by heka1203 on 2017-04-01.
 */

public class CacheController {

    //TODO: separate from this class >
    public static final Logger logger = Logger.getLogger(CacheController.class.getName());
    private MongoClient mongoClient = new MongoClient("localhost", 27017);
    private MongoDatabase mongoDatabase = mongoClient.getDatabase("streamingdata");
    private static final String CACHECONFIG_COLLECTION_NAME = "cacheconfigs";
    private static final String CACHEKEYS_COLLECTION_NAME = "cachekeys";

    private CacheRepository cacheRepository;

    public CacheController() throws CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException {
        initialize();
    }
    public Object create(CacheConfig cacheConfig) throws CacheAlreadyExistsException { //DB
        cacheRepository.addCache(new CacheImpl(cacheConfig));
        mongoDatabase.getCollection(CACHECONFIG_COLLECTION_NAME).insertOne(Document.parse(toJson(cacheConfig))); //handle db error?
        return cacheConfig;
    }
    public Object edit(CacheConfig cacheConfig) throws CacheNotFoundException { //DB
        String cacheName = cacheConfig.getName();
        Cache cache = cacheRepository.getCache(cacheName);
        mongoDatabase.getCollection(CACHECONFIG_COLLECTION_NAME).findOneAndUpdate(new Document("name", cacheName), new Document("$set", Document.parse(toJson(cacheConfig))));
        synchronized (cache){
            cache.setCacheConfig(cacheConfig);
        }
        return cacheConfig;
    }
    public Object delete(String cacheName) throws CacheNotFoundException { //DB
        mongoDatabase.getCollection(CACHECONFIG_COLLECTION_NAME).findOneAndDelete(new Document("name", cacheName));
        return cacheRepository.deleteCache(cacheName).getCacheConfig();
    }
    public Object get(String cacheName) throws CacheNotFoundException {
        return cacheRepository.getCache(cacheName).getCacheConfig();
    }

    public Object putKey(String cacheName, TreeMap<String,String> key) throws CacheNotFoundException, RequiresValidDateException, InvalidKeyException { //DB

        Cache cache = cacheRepository.getCache(cacheName);
        LocalDateTime now = LocalDateTime.now();
        int expireDays = cache.getCacheConfig().getExpireDays();
        LocalDateTime expireDateTime = now.plusDays(expireDays);
        mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).insertOne(Document.parse(toJson(key)).append("createdAt", now.toString()).append("expireAt", expireDateTime.toString()));
        return cache.put(key, now, 1);
    }
    public Object getPointEntry(String cacheName, String date, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(date);
        return cacheRepository.getCache(cacheName).pointGet(key, LocalDateTime.parse(date));
    }
    public Object getPointsEntry(String cacheName, String startDate, String endDate, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(startDate);
        validateISODate(endDate);
        return cacheRepository.getCache(cacheName).pointsGet(key, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    public Object getRangeEntry(String cacheName, String startDate, String endDate, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(startDate);
        validateISODate(endDate);
        return cacheRepository.getCache(cacheName).rangeGet(key, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }
    public Object getTopEntry(String cacheName, int days) throws CacheNotFoundException {
        return cacheRepository.getCache(cacheName).topGet(days);
    }

    private void initialize() throws InvalidKeyException {

        FindIterable<Document> cacheConfigs = mongoDatabase.getCollection("cacheconfigs").find().projection(new Document("id", 0));
        Map<String, Cache> caches = new HashMap<>();
        for(Document cc : cacheConfigs){

            CacheConfig cacheConfig = fromCacheConfig(cc.toJson());
            Cache cache = new CacheImpl(cacheConfig);
            String cacheName = cacheConfig.getName();
            FindIterable<Document> cacheKeys = mongoDatabase.getCollection("cachekeys").find(new Document("name", cacheName));

            for(Document cacheKey : cacheKeys){
                Key key = fromKey(cacheKey.toJson());
                cache.put(key.getKey(), key.getCreatedAt(), 1);
            }
            caches.put(cacheName, cache);

        }
        logger.info(String.format("Loaded %d caches.", caches.size()));

        this.cacheRepository = new CacheRepository(caches);
    }



}
