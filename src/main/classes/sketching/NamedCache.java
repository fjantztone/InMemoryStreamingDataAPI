package sketching;

import exceptions.RequiresValidDateException;
import hashing.FNV;
import utils.ParseUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */

public class NamedCache implements Cache<CacheEntry>{
    private CountMinSketch cms;
    private CountMinRange cmr;
    private CacheConfig cacheConfig;
    private static Logger logger = Logger.getLogger(NamedCache.class.getName());

    public NamedCache(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
        final int width = 1 << 14;
        final int depth = 4;
        final int numberOfSketches = (int)Math.ceil(cacheConfig.getExpireDays() / Math.log(2));
        this.cms = new CountMinSketch(width, depth, new FNV());
        this.cmr = new CountMinRange(width, depth, numberOfSketches);
    }

    @Override
    public List<CacheEntry> get(TreeMap<String,String> key, String filter)  {

        switch(filter){
            case "point":
                return pointGet(key);
            case "points":
                return pointsGet(key);
            case "range":
                return rangeGet(key);
            case "top":
                return null;
            default:
                return null;
        }

    }
    public List<CacheEntry> pointGet(TreeMap<String,String> key) {
        return Arrays.asList(put(key, 0));
    }
    public List<CacheEntry> pointsGet(TreeMap<String,String> key) {

        List<CacheEntry> cacheEntries = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(key.remove("STARTDATE"));
        LocalDate endDate = LocalDate.parse(key.remove("ENDDATE"));
        int daysBetween = (int) ChronoUnit.DAYS.between(startDate, endDate);
        for(int plusDays = 0; plusDays <= daysBetween; plusDays++){
            LocalDate current = startDate.plusDays(plusDays);
            key.put("DATE", current.toString());
            cacheEntries.add(put(key, 0));
        }
        return cacheEntries;

    }
    public List<CacheEntry> rangeGet(TreeMap<String,String> key) {

        List<CacheEntry> cacheEntries = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(key.remove("STARTDATE"));
        LocalDate endDate = LocalDate.parse(key.remove("ENDDATE"));
        LocalDate createdDate = cacheConfig.getCreatedDate(); //cache created date

        int start = (int) ChronoUnit.DAYS.between(createdDate, startDate); //TODO: APP_START_DATE should be this caches creation date?
        int end = (int) ChronoUnit.DAYS.between(createdDate, endDate);
        int rangeFrequency = cmr.get(key, start, end);
        //putting the ISO-date back for nicer client response
        key.put("STARTDATE", startDate.toString());
        key.put("ENDDATE", endDate.toString());

        return Arrays.asList(new CacheEntry(key, rangeFrequency));

    }
    @Override
    public CacheEntry put(TreeMap<String,String> key, int amount) {
        LocalDate localDate = LocalDate.parse(key.remove("DATE"));
        LocalDate createdDate = cacheConfig.getCreatedDate();

        int daysBetween = (int) ChronoUnit.DAYS.between(createdDate, localDate);
        //TODO: SPLIT FIELDS EQUAL TO LEVELS??
        int pointFrequency = cms.put(key, daysBetween, amount);
        cmr.put(key, daysBetween, amount); //bottleneck?

        key.put("DATE", localDate.toString()); //putting the ISO-date back for nicer client response
        return new CacheEntry(key, pointFrequency);


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
