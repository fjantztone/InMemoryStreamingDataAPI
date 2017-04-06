import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.RequiredDateException;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.InputField;
import spark.QueryParamsMap;
import spark.servlet.SparkApplication;
import spark.utils.SparkUtils;
import utils.ParseUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */


public class CacheMiddleWare {
    //Should be read from file later on.
    protected CacheRepository cacheRepository;
    public static Logger logger = Logger.getLogger(CacheMiddleWare.class.getName());

    public CacheMiddleWare() throws RequiredDateException {
        cacheRepository = new CacheRepository();
    }

    public Object create(String cacheConfig) throws CacheNotFoundException, CacheAlreadyExistsException {
        Cache cache = cacheRepository.createCache(cacheConfig);
        System.out.println(cache);
        CacheConfig _cacheConfig = cacheRepository.addCache(cache);

        return _cacheConfig;
    }
    public Object edit(String cacheConfig) throws CacheNotFoundException {
        return cacheRepository.editCache(cacheConfig);
    }

    public Object putFile(InputStream is, String cacheName) throws IOException, CacheNotFoundException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            Cache cache = cacheRepository.getCache(cacheName);
            List<InputField> fileFields = cache.getCacheConfig().getFileFields();
            br.lines().map(line -> ParseUtil.parseFileRow(line, fileFields)).filter(Objects::nonNull).forEach(key -> {
                try {
                    cacheRepository.addCacheKey(key, cache);
                } catch (RequiredDateException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                }
            });
            return new ResponseText("Uploaded file successfully");
        }
    }
    public Object putKey(String key, String cacheName) throws CacheNotFoundException, RequiredDateException {
        Cache cache = cacheRepository.getCache(cacheName);
        List<InputField> jsonFields = cache.getCacheConfig().getJsonFields();
        //nullcheck^
        TreeMap<String,String> parsedKey = ParseUtil.parseJson(key, jsonFields);
        if(parsedKey != null){
            System.out.println("Putting key: " + parsedKey);
            return cacheRepository.addCacheKey(parsedKey, cache);
        }
        return null;
    }
    public Object getEntry(QueryParamsMap queryParamsMap, String cacheName, String filter) throws RequiredDateException, CacheNotFoundException {
        Cache cache = cacheRepository.getCache(cacheName);
        TreeMap<String,String> parsedKey = ParseUtil.parseQueryParams(queryParamsMap);
        System.out.println("Requesting key: " + parsedKey);

        return cache.get(parsedKey, filter);

    }


}
