package controllers;

import caching.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;


import exceptions.*;
import models.*;
import models.Key;
import org.bson.Document;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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

    private CacheRepository cacheRepository;

    public CacheController() throws CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException, IOException {
        initialize();
    }
    public Object create(CacheConfig cacheConfig) throws CacheAlreadyExistsException, JsonProcessingException { //DB
        String cacheName = cacheConfig.getName();
        cacheRepository.addCache(new CacheImpl(cacheConfig));
        mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).insertOne(Document.parse(toJson(cacheConfig)));
        mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).insertOne(new Document("name", cacheName).append("keys", new ArrayList<>())); //<-- hmm
        return cacheConfig;
    }
    public Object edit(CacheConfig cacheConfig) throws CacheNotFoundException, JsonProcessingException { //DB
        String cacheName = cacheConfig.getName();
        Cache cache = cacheRepository.getCache(cacheName);
        mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).findOneAndUpdate(new Document("name", cacheName), new Document("$set", Document.parse(toJson(cacheConfig))));

        synchronized (cache){
            cache.setCacheConfig(cacheConfig);
        }
        return cacheConfig;
    }
    public Object delete(String cacheName) throws CacheNotFoundException { //DB
        mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).deleteOne(new Document("name", cacheName));
        mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).deleteOne(new Document("name", cacheName));
        return cacheRepository.deleteCache(cacheName).getCacheConfig();
    }
    public Object get(String cacheName) throws CacheNotFoundException {
        return cacheRepository.getCache(cacheName).getCacheConfig();
    }

    public Object putKey(String cacheName, TreeMap<String,String> key) throws CacheNotFoundException, RequiresValidDateException, InvalidKeyException, JsonProcessingException {

        LocalDateTime now = LocalDateTime.now();
        Cache cache = cacheRepository.getCache(cacheName);
        if(cache.hasExpired(now)){ //lazy expire
            CacheConfig cacheConfig = cache.getCacheConfig();
            delete(cacheName); //in cc hashmap
            cache = new CacheImpl(cacheConfig);
        }
        Key keyObj = new Key(key, now);

        Document filter = new Document("name", cacheName);
        Document update = new Document("$push", new Document("keys", Document.parse(toJson(keyObj))));
        mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).updateOne(filter, update);
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

    private void initialize() throws InvalidKeyException, IOException {

        FindIterable<Document> cacheConfigs = mongoDatabase.getCollection(CACHECONFIGS_COLLECTION_NAME).find();
        Map<String, Cache> caches = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for(Document dcc : cacheConfigs){
            CacheConfig cacheConfig = objectMapper.readValue(dcc.toJson(), CacheConfig.class);
            Cache cache = new CacheImpl(cacheConfig);
            String cacheName = cacheConfig.getName();
            Document dcck = mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).find(new Document("name", cacheName)).first();
            @SuppressWarnings("unchecked")
            List<Document> cacheKeys = (List<Document>)dcck.get("keys");
            for(Document cacheKey : cacheKeys){
                Key key = objectMapper.readValue(cacheKey.toJson(), Key.class);
                cache.put(key.getKey(), key.getCreatedAt(), 1);
            }
            logger.info(String.format("Loaded %d keys into %s.", cacheKeys.size(), cacheName));
            caches.put(cacheName, cache);

        }
        logger.info(String.format("Loaded %d caches.", caches.size()));

        this.cacheRepository = new CacheRepository(caches);
    }



}
