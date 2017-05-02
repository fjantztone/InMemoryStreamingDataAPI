package controllers;

import caching.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


import exceptions.*;
import models.*;
import models.Key;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static utils.DateUtil.*;
import static utils.JsonUtil.*;

/**
 * Created by heka1203 on 2017-04-01.
 */

public class CacheController {

    //TODO: separate from this class >
    public static final Logger logger = Logger.getLogger(CacheController.class.getName());
    private static final String DATABASE_NAME = "streamingdata";
    private static final String CACHECONFIGS_COLLECTION_NAME = "cacheconfigs";
    private static final String CACHEKEYS_COLLECTION_NAME = "cachekeys";
    private final MongoDatabase mongoDatabase = new MongoClient().getDatabase(DATABASE_NAME);
    private final Map<String, Cache> caches = new ConcurrentHashMap<>(new HashMap<>());
    private CacheRepository cacheRepository;

    public CacheController() throws CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException, IOException {
        initialize();
    }
    public Object create(CacheConfig cacheConfig) throws CacheAlreadyExistsException, JsonProcessingException { //DB
        String cacheName = cacheConfig.getName();

        Cache cache = caches.putIfAbsent(cacheName, new CacheImpl(cacheConfig));
        if(cache != null) throw new CacheAlreadyExistsException(String.format("A cache with name: %s already exists. Try another cache name.", cacheName));

        mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).insertOne(Document.parse(toJson(cacheConfig)));
        mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).insertOne(new Document("name", cacheName).append("keys", new ArrayList<>())); //<-- hmm
        return cacheConfig;
    }
    public Object edit(CacheConfig cacheConfig) throws CacheNotFoundException, JsonProcessingException { //DB
        String cacheName = cacheConfig.getName();
        Cache cache = caches.computeIfPresent(cacheName, (k,c) -> {c.setCacheConfig(cacheConfig); return c;});
        if(cache == null) throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));

        mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).findOneAndUpdate(new Document("name", cacheName), new Document("$set", Document.parse(toJson(cacheConfig))));
        return cacheConfig;
    }
    public Object delete(String cacheName) throws CacheNotFoundException { //DB
        Cache cache = caches.remove(cacheName);
        if(cache == null) throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));

        mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).deleteOne(new Document("name", cacheName));
        mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).deleteOne(new Document("name", cacheName));
        return cache.getCacheConfig();
    }
    public Object get(String cacheName) throws CacheNotFoundException {
        Cache cache = caches.get(cacheName);
        if(cache == null) throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));
        return cache.getCacheConfig();
    }

    public Object putKey(String cacheName, TreeMap<String,String> key) throws CacheNotFoundException, RequiresValidDateException, InvalidKeyException, JsonProcessingException, CacheAlreadyExistsException {
        LocalDateTime now = LocalDateTime.now();
        return putKey(cacheName, key, now);
    }
    public Object putKey(String cacheName, TreeMap<String,String> key, LocalDateTime now) throws CacheNotFoundException, JsonProcessingException {
        //check key here instead?

        Cache cache = caches.computeIfPresent(cacheName, (k,c) -> {
            if(c.hasExpired(now)){
                logger.info(String.format("%s has expired and is reset.", cacheName));
                CacheConfig cacheConfig = c.getCacheConfig();
                cacheConfig.setCreatedAt(now);
                c = new CacheImpl(cacheConfig);
            }
            try {
                c.put(key, now, 1);
                return c;
            } catch (InvalidKeyException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        });
        if(cache == null)
            throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));

        //Key keyObj = new Key(key, cacheName, now);
        //mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).insertOne(Document.parse(toJson(keyObj)));

        return cache.pointGet(key, now);
    }
    public Object getPointEntry(String cacheName, String date, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(date);
        Cache cache = caches.get(cacheName);
        if(cache == null)
            throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));

        return cache.pointGet(key, LocalDateTime.parse(date));
    }
    public Object getPointsEntry(String cacheName, String startDate, String endDate, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(startDate);
        validateISODate(endDate);
        Cache cache = caches.get(cacheName);
        if(cache == null)
            throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));

        return cache.pointsGet(key, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }

    public Object getRangeEntry(String cacheName, String startDate, String endDate, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(startDate);
        validateISODate(endDate);
        Cache cache = caches.get(cacheName);
        if(cache == null)
            throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));

        return cache.rangeGet(key, LocalDateTime.parse(startDate), LocalDateTime.parse(endDate));
    }
    public Object getTopEntry(String cacheName, int days) throws CacheNotFoundException {
        Cache cache = caches.get(cacheName);
        if(cache == null)
            throw new CacheNotFoundException(String.format("Cache with name: %s does not exist. You can create a new cache at route: /api/cache.", cacheName));

        return cache.topGet(days);
    }

    private void initialize() throws InvalidKeyException, IOException {
        FindIterable<Document> cacheConfigs = mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).find();
        ObjectMapper objectMapper = new ObjectMapper();

        for(Document dcc : cacheConfigs){

            CacheConfig cacheConfig = objectMapper.readValue(dcc.toJson(), CacheConfig.class);
            Cache cache = new CacheImpl(cacheConfig);
            String cacheName = cacheConfig.getName();
            if(!cacheName.equals("tests")){
                FindIterable<Document> cacheKeys = mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).find(new Document("cacheName", cacheName)).projection(new Document("_id", 0));
                int numberOfKeys = 0;
                for(Document cacheKey : cacheKeys){
                    Key key = objectMapper.readValue(cacheKey.toJson(), Key.class);
                    cache.put(key.getKey(), key.getCreatedAt(), 1);
                    numberOfKeys++;
                }

                logger.info(String.format("Loaded %d keys into %s.", numberOfKeys, cacheName));

                caches.put(cacheName, cache);

            }

        }
        logger.info(String.format("Loaded %d caches.", caches.size()));

    }



}
