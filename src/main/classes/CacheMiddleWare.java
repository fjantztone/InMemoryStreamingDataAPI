import exceptions.*;
import sketching.Cache;
import sketching.CacheConfig;
import sketching.InputField;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import utils.ParseUtil;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.*;
import java.lang.instrument.UnmodifiableClassException;
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

    public CacheMiddleWare() throws RequiresValidDateException {
        cacheRepository = new CacheRepository();
    }

    public Object create(Request req, Response res) throws CacheNotFoundException, CacheAlreadyExistsException {
        Cache cache = cacheRepository.createCache(req.body());
        CacheConfig cacheConfig = cacheRepository.addCache(cache);

        return cacheConfig;
    }
    public Object edit(Request req, Response res) throws CacheNotFoundException {
        return cacheRepository.editCache(req.body());
    }

    public Object putFile(Request req, Response res) throws IOException, CacheNotFoundException, ServletException, RequiresValidDateException {
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp")); //For test
        Part file = req.raw().getPart("uploaded_file");
        if(file == null) throw new FileNotFoundException("No file was found. Make sure that the input field is named: 'uploaded_file'.");

        try (BufferedReader br = new BufferedReader(new InputStreamReader(req.raw().getPart("uploaded_file").getInputStream()))) {
            String cacheName = req.params(":name");
            logger.log(Level.INFO, String.format("Trying to upload a file to cache: %s!", cacheName));
            Cache cache = cacheRepository.getCache(cacheName);
            List<InputField> fileFields = cache.getCacheConfig().getFileFields();
            int numberOfKeys = 0;
            for (String line; (line = br.readLine()) != null; ) {
                TreeMap<String, String> key = ParseUtil.parseFileRow(line, fileFields);
                if (key != null){
                    cacheRepository.addCacheKey(key, cache);
                    ++numberOfKeys;
                }

                //ignore
            }
            return new ResponseText(String.format("File was uploaded successfully. %d keys were inserted to cache.", numberOfKeys)); //TODO: REturn number of keys inserted?
        }

    }
    public Object putKey(Request req, Response res) throws CacheNotFoundException, RequiresValidDateException {
        Cache cache = cacheRepository.getCache(req.params(":name"));
        List<InputField> jsonFields = cache.getCacheConfig().getJsonFields();
        //nullcheck^
        TreeMap<String,String> parsedKey = ParseUtil.parseJson(req.body(), jsonFields); //Block with filter if not as expected
        System.out.println("Putting key: " + parsedKey);
        return cacheRepository.addCacheKey(parsedKey, cache);

    }
    public Object getEntry(Request req, Response res) throws CacheNotFoundException, FilterNotFoundException, RequiresDateException, RequiresValidDateException {
        Cache cache = cacheRepository.getCache(req.params(":name"));
        TreeMap<String,String> parsedKey = ParseUtil.parseQueryParams(req.queryMap(), req.params(":filter"));
        //^nullcheck
        return cache.get(parsedKey, req.params(":filter"));

    }


}
