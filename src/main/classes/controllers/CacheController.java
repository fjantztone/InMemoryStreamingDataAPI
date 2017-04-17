package controllers;

import caching.Cache;
import caching.CacheRepository;
import exceptions.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */


public class CacheController {
    //Should be read from file later on.
    protected CacheRepository cacheRepository;
    public static Logger logger = Logger.getLogger(CacheController.class.getName());

    public CacheController() throws CacheAlreadyExistsException, CacheNotFoundException {
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

    public Object putKey(String cacheName, TreeMap<String,String> key) throws CacheNotFoundException, RequiresValidDateException, InvalidKeyException {

        Cache cache = cacheRepository.getCache(cacheName);
        validateKey(key, cache);
        return cache.put(key, LocalDate.now(), 1);
    }
    public Object getPointEntry(String cacheName, String date, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(date);
        Cache cache = cacheRepository.getCache(cacheName);

        return cache.pointGet(key, LocalDate.parse(date));
    }
    public Object getPointsEntry(String cacheName, String startDate, String endDate, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(startDate);
        validateISODate(endDate);
        Cache cache = cacheRepository.getCache(cacheName);

        return cache.pointsGet(key, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    public Object getRangeEntry(String cacheName, String startDate, String endDate, TreeMap<String,String> key) throws RequiresValidDateException, CacheNotFoundException {
        validateISODate(startDate);
        validateISODate(endDate);
        Cache cache = cacheRepository.getCache(cacheName);

        return cache.rangeGet(key, LocalDate.parse(startDate), LocalDate.parse(endDate));
    }
    public Object getTopEntry(String cacheName, int days) throws CacheNotFoundException {
        Cache cache = cacheRepository.getCache(cacheName);
        return cache.topGet(days);
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
