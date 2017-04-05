import com.google.gson.*;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.CacheField;
import sketching.NamedCache;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import utils.JsonUtil;
import utils.ParseUtil;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by heka1203 on 2017-04-01.
 */


public class CacheMiddleWare {
    //Should be read from file later on.
    protected CacheRepository cacheRepository = new CacheRepository();
    public static Logger logger = Logger.getLogger(CacheMiddleWare.class.getName());

    public Object create(String cacheConfig){
        Cache cache = cacheRepository.createCache(cacheConfig);
        CacheConfig _cacheConfig = cacheRepository.addCache(cache);
        return _cacheConfig;
    }
    public Object edit(String cacheConfig){
        return cacheRepository.editCache(cacheConfig);
    }
    public Object putFile(InputStream is, String cacheName) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            List<CacheField> allowableCacheFields = cacheRepository.getAllowableCacheFields(cacheName);
            br.lines().map(line -> ParseUtil.parseFileEntry(line, allowableCacheFields)).filter(Objects::nonNull).forEach(key -> cacheRepository.addCacheKey(key, cacheName));
            return new ResponseText("Uploaded file successfully");
        }
    }
    public Object putKey(String key, String cacheName){

        List<CacheField> allowableCacheFields = cacheRepository.getAllowableCacheFields(cacheName);
        //nullcheck^
        key = ParseUtil.parseJson(key, allowableCacheFields);
        if(key != null){
            System.out.println("Putting key: " + key);
            return cacheRepository.addCacheKey(key, cacheName);
        }
        return null;
    }
    public Object getEntry(QueryParamsMap queryParamsMap, String cacheName, String filter){
        List<CacheField> allowableCacheFields = cacheRepository.getAllowableCacheFields(cacheName);
        //nullcheck^
        String key = ParseUtil.parseQueryParamsMap(queryParamsMap, allowableCacheFields);
        if(key != null){
            System.out.println("Getting key: " + key);
            return cacheRepository.getCacheEntry(key, cacheName, filter);
        }
        return null;
    }


}
