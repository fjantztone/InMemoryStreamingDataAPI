package db;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.NamedCache;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by heka1203 on 2017-04-05.
 */
public class CacheRepository {
    MongoClient mongoClient = new MongoClient("localhost", 27017);
    MongoDatabase database = mongoClient.getDatabase("streamingdata");

    public List<Cache> caches = new ArrayList<>();

    public CacheRepository(){
    }

    public void addCache(String cacheConfig){
        Document _cacheConfig = Document.parse(cacheConfig);
        database.getCollection("cacheConfigs").insertOne(_cacheConfig);
    }
    public void addCacheKey(String key, String cacheName){
        Document _key = Document.parse(key);
        _key.append("cacheName", cacheName);
        database.getCollection("cacheKeys").insertOne(_key);
    }
    protected void initialize(){
        FindIterable<Document> documents = database.getCollection("cacheConfigs").find();
        for(Document document : documents){
            String cacheConfig = document.toJson();
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(CacheConfig.class, new CacheConfigDeserializer());
            Gson gson = gsonBuilder.create();

            CacheConfig cc = gson.fromJson(json, CacheConfig.class);
            NamedCache cache = new NamedCache()
        }
    }

}
