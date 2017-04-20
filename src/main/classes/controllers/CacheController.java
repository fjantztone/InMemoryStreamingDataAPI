package controllers;

import caching.*;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import exceptions.*;
import org.bson.Document;
import static utils.JsonUtil.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;
import static utils.DateUtil.validateISODate;

/**
 * Created by heka1203 on 2017-04-01.
 */

//TODO: Fix cachekeys table
public class CacheController {

    private MongoClient mongoClient = new MongoClient("localhost", 27017);
    private MongoDatabase mongoDatabase = mongoClient.getDatabase("streamingdata");

    protected CacheRepository cacheRepository;

    public static Logger logger = Logger.getLogger(CacheController.class.getName());

    public CacheController() throws CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException {
        cacheRepository = new CacheRepository();
    }
    public Object create(CacheConfig cacheConfig) throws CacheAlreadyExistsException { //DB
        cacheRepository.addCache(new CacheImpl(cacheConfig));
        return cacheConfig;
    }
    public Object edit(CacheConfig cacheConfig) throws CacheNotFoundException { //DB
        Cache cache = cacheRepository.getCache(cacheConfig.getName());
        synchronized (cache){
            cache.setCacheConfig(cacheConfig);
        }
        return cacheConfig;
    }
    public Object delete(String cacheName) throws CacheNotFoundException { //DB
        return cacheRepository.deleteCache(cacheName).getCacheConfig();
    }
    public Object get(String cacheName) throws CacheNotFoundException {
        return cacheRepository.getCache(cacheName).getCacheConfig();
    }

    public Object putKey(String cacheName, TreeMap<String,String> key) throws CacheNotFoundException, RequiresValidDateException, InvalidKeyException {
        return cacheRepository.getCache(cacheName).put(key, LocalDateTime.now(), 1);
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

    protected void initialize() { //edits must be done in database aswell..

        FindIterable<Document> cacheConfigs = mongoDatabase.getCollection("cacheconfigs").find().projection(Document.parse("{'_id' : 0}"));

        for(Document cc : cacheConfigs){
            //convert and insert keys
            CacheConfig cacheConfig = fromCacheConfig(cc.toJson());
            Cache cache = new CacheImpl(cacheConfig);
            @SuppressWarnings("unchecked")
            List<Document> docs = (List<Document>)cc.get("data");
            for(Document doc : docs){
                Key key = toKey(doc.toJson());
                cache.put(key.getKey(), key.getCreatedAt(), 1);
            }
            System.out.printf("Loaded %d keys into cache.\n", docs.size());
        }
    }



}
