package sketching;

import com.google.gson.JsonObject;
import hashing.FNV;
import utils.ParseUtil;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */

public class NamedCache implements Cache<JsonObject>{
    private CountMinSketch cms;
    private CacheConfig cacheConfig;
    private static Logger logger = Logger.getLogger(NamedCache.class.getName());

    public NamedCache(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
        this.cms = new CountMinSketch((int)Math.ceil(cacheConfig.getFrequencyQuery().getWindow() / Math.log(2)), 4, new FNV());
    }

    @Override
    public JsonObject topQuery() {
        return null;
    }

    @Override
    //QueryMap?
    public JsonObject pointQuery(Object key) {
        if(key instanceof JsonObject){
            JsonObject keyObj = (JsonObject)key;
            int frequency = cms.get(key);
            JsonObject obj = new JsonObject();
            obj.addProperty("frequency", frequency);
            obj.addProperty("resource", keyObj.toString());
            return obj;
        }
        else
            throw new RuntimeException("Invalid input format");

    }

    @Override
    public JsonObject rangeQuery(Object key) {
        return null;
    }

    @Override
    public boolean put(Object key, int value) {
        if(key instanceof JsonObject){
            JsonObject obj = (JsonObject)key;
            JsonObject parsedObj = ParseUtil.parse(obj, cacheConfig.getCacheFields());
            return cms.put(parsedObj, 1) != -1;
        }
        if(key instanceof String){
            String obj = (String)key;
            JsonObject parsedObj = ParseUtil.parse(obj, cacheConfig.getCacheFields());
            return cms.put(parsedObj, 1) != -1;
        }
        return false;
    }
    @Override
    public boolean put(Object key){
        return put(key, 1);
    }

    @Override
    public String getName() {
        return cacheConfig.getCacheName();
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
    }

    @Override
    public CacheConfig getCacheConfig() {
        return this.cacheConfig;
    }

    public String toString(){
        return cacheConfig.toString();
    }

}
