package caching;

import com.google.gson.Gson;
import exceptions.*;
import spark.Request;
import spark.Response;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */


public class CacheMiddleWare {
    //Should be read from file later on.
    protected CacheRepository cacheRepository;
    public static Logger logger = Logger.getLogger(CacheMiddleWare.class.getName());

    public CacheMiddleWare() throws CacheAlreadyExistsException, CacheNotFoundException {
        cacheRepository = new CacheRepository();
    }

    public Object create(String cacheConfig) throws CacheNotFoundException, CacheAlreadyExistsException { //String cacheConfig
        return cacheRepository.createCache(cacheConfig).getCacheConfig();
    }
    public Object edit(String cacheConfig) throws CacheNotFoundException { //String cacheConfig
        return cacheRepository.editCache(cacheConfig).getCacheConfig();
    }
    public Object delete(String cacheName) throws CacheNotFoundException { //String cacheName
        return cacheRepository.deleteCache(cacheName).getCacheConfig();
    }
    public Object get(String cacheName) throws CacheNotFoundException {
        return cacheRepository.getCache(cacheName).getCacheConfig();
    }

    public Object putKey(String cacheName, String key) throws CacheNotFoundException, RequiresValidDateException, InvalidKeyException {
        TreeMap<String,String> _key = new Gson().fromJson(key, TreeMap.class);
        Cache cache = cacheRepository.getCache(cacheName);
        validateKey(_key, cache);
        return cache.put(_key, LocalDate.now(), 1);
    }
    public Object getPointEntry(String cacheName, String key, String date) throws RequiresValidDateException, CacheNotFoundException {
        ///cache/:name/filter/point/date/:date/key/:key
        validateISODate(date);

        TreeMap<String,String> _key = new Gson().fromJson(key, TreeMap.class); //key is expected to be valid.
        Cache cache = cacheRepository.getCache(cacheName);

        return cache.pointGet(_key, LocalDate.parse(date));
    }
    public Object getPointsEntry(Request req, Response res){
        throw new UnsupportedOperationException();
    }

    public Object getRangeEntry(String cacheName, String startDate, String endDate, String key){
        throw new UnsupportedOperationException();
    }
    public Object getTopEntry(String cacheName, int days){
        throw new UnsupportedOperationException();
    }

    public void validateISODate(String date) throws RequiresValidDateException {
        try{
            LocalDate localDate = LocalDate.parse(date);
        }
        catch(DateTimeParseException e){
            throw new RequiresValidDateException("The date must be in ISO-8601 format.");
        }
    }
    public void validateKey(TreeMap<String,String> key, Cache cache) throws InvalidKeyException { //May be a bad place
        List<String> attributes = cache.getCacheConfig().getAttributes();
        key.keySet().retainAll(attributes);
        if(key.size() != attributes.size())
            throw new InvalidKeyException(String.format("The provided key does not match the required attributes (%s).", attributes));
    }

}
