package sketching;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hashing.FNV;
import utils.ParseUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */

public class NamedCache implements Cache<CacheEntry>{
    private CountMinSketch cms;
    private CacheConfig cacheConfig;
    private static Logger logger = Logger.getLogger(NamedCache.class.getName());

    public NamedCache(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
        this.cms = new CountMinSketch((int)Math.ceil(cacheConfig.getFrequencyQuery().getWindow() / Math.log(2)), 4, new FNV());
    }

    @Override
    public List<CacheEntry> get(String key, String filter) {
        JsonParser parser = new JsonParser();
        JsonElement _key = parser.parse(key);
        JsonObject jsonKey = _key.getAsJsonObject();

        List<CacheEntry> cacheEntries = new ArrayList<>();
        switch(filter){
            case "point":
                cacheEntries.add(put(key, 0));
                return cacheEntries;
            case "points":{
                try{
                    LocalDate startDate = LocalDate.parse(jsonKey.remove("STARTDATE").getAsString());
                    LocalDate endDate = LocalDate.parse(jsonKey.remove("ENDDATE").getAsString());
                    int daysBetween = (int) ChronoUnit.DAYS.between(startDate, endDate);
                    for(int plusDays = 0; plusDays <= daysBetween; plusDays++){
                        LocalDate current = startDate.plusDays(plusDays);
                        jsonKey.addProperty("DATE", current.toString());
                        cacheEntries.add(put(key, 0));
                    }
                    return cacheEntries;
                } catch(DateTimeParseException e){
                    //TODO: throw points required start and end date exception
                    throw e;
                }

            }
            case "range":{
                try{
                    LocalDate startDate = LocalDate.parse(jsonKey.get("STARTDATE").getAsString());
                    LocalDate endDate = LocalDate.parse(jsonKey.get("ENDDATE").getAsString());
                    int daysBetween = (int) ChronoUnit.DAYS.between(startDate, endDate);
                    //CMR RANGE QUERY

                } catch(DateTimeParseException e){
                    //TODO: throw points required start and end date exception
                    throw e;
                }


            }
        }


        return null;
    }

    @Override
    public CacheEntry put(String key, int value) {
        JsonParser parser = new JsonParser();
        JsonElement _key = parser.parse(key);
        JsonObject jsonKey = _key.getAsJsonObject();
        //TODO: Extract datediff etc.

        return new CacheEntry(jsonKey, Integer.valueOf(cms.put(jsonKey, 1)));

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

}
