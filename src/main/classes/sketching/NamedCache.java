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
        final int numberOfSketches = (int)Math.ceil(cacheConfig.getFrequencyQuery().getWindow() / Math.log(2));
        this.cms = new CountMinSketch(width, depth, new FNV());
        this.cmr = new CountMinRange(width, depth, numberOfSketches);
    }

    @Override
    public List<CacheEntry> get(TreeMap<String,String> key, String filter) throws RequiresValidDateException {

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
    public List<CacheEntry> pointGet(TreeMap<String,String> key) throws RequiresValidDateException {
        return Arrays.asList(put(key, 0));
    }
    public List<CacheEntry> pointsGet(TreeMap<String,String> key) throws RequiresValidDateException {

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
        int start = (int) ChronoUnit.DAYS.between(ParseUtil.APP_START_DATE, startDate);
        int end = (int) ChronoUnit.DAYS.between(ParseUtil.APP_START_DATE, endDate);
        return Arrays.asList(new CacheEntry(key, cmr.get(key, start, end)));

    }
    @Override
    public CacheEntry put(TreeMap<String,String> key, int amount) {

        LocalDate localDate = LocalDate.parse(key.remove("DATE"));
        int daysBetween = (int) ChronoUnit.DAYS.between(CountMinSketch.APP_START_DATE, localDate);
        //TODO: SPLIT FIELDS??
        int pointFrequency = cms.put(key, daysBetween, amount);
        cmr.put(key, daysBetween, amount);
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
