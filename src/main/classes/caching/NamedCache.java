package caching;

import exceptions.TopListNotFoundException;
import hashing.FNV;
import sketches.CountMinRange;
import sketches.CountMinSketch;
import sketches.SlidingWindowTopList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */

//TODO: FIX NAMED TOPLIST IN CACHE CONFIG AND SEPARATE

public class NamedCache implements Cache<CacheEntry>{
    private CountMinSketch cms;
    private CountMinRange cmr;
    private HashMap<String,SlidingWindowTopList> swts;
    private CacheConfig cacheConfig;
    private static Logger logger = Logger.getLogger(NamedCache.class.getName());

    public NamedCache(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
        initializeSketches();
    }

    @Override
    public List<CacheEntry> get(TreeMap<String,String> key, String filter) throws TopListNotFoundException {

        switch(filter){
            case "point":
                return pointGet(key);
            case "points":
                return pointsGet(key);
            case "range":
                return rangeGet(key);
            case "top":
                return topGet(key); //topGet
            default:
                return null;
        }

    }
    public List<CacheEntry> pointGet(TreeMap<String,String> key) {
        return Arrays.asList(get(key));
    }
    public List<CacheEntry> pointsGet(TreeMap<String,String> key) {

        List<CacheEntry> cacheEntries = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(key.remove("STARTDATE"));
        LocalDate endDate = LocalDate.parse(key.remove("ENDDATE"));
        int daysBetween = (int) ChronoUnit.DAYS.between(startDate, endDate);
        for(int plusDays = 0; plusDays <= daysBetween; plusDays++){
            LocalDate current = startDate.plusDays(plusDays);
            key.put("DATE", current.toString());
            cacheEntries.add(get(key));
        }
        return cacheEntries;

    }
    public List<CacheEntry> rangeGet(TreeMap<String,String> key) {

        List<CacheEntry> cacheEntries = new ArrayList<>();
        LocalDate startDate = LocalDate.parse(key.remove("STARTDATE"));
        LocalDate endDate = LocalDate.parse(key.remove("ENDDATE"));
        LocalDate createdDate = cacheConfig.getCreatedDate(); //cache created date

        int start = (int) ChronoUnit.DAYS.between(createdDate, startDate);
        int end = (int) ChronoUnit.DAYS.between(createdDate, endDate);
        int rangeFrequency = cmr.get(key, start, end);
        //putting the ISO-date back for nicer client response
        key.put("STARTDATE", startDate.toString());
        key.put("ENDDATE", endDate.toString());

        return Arrays.asList(new CacheEntry(key, rangeFrequency));

    }
    public List<CacheEntry> topGet(TreeMap<String,String> key) throws TopListNotFoundException {

        int days = Integer.parseInt(key.get("DAYS"));
        String name = key.get("NAME");
        SlidingWindowTopList swt = swts.get(key.get("NAME"));
        if(swt == null) throw new TopListNotFoundException(String.format("There exists no toplist with name: %s.", name));
        return swt.toCacheEntries(days);

    }
    @Override
    public List<CacheEntry> put(TreeMap<String,String> key, int amount) {
        LocalDate localDate = LocalDate.parse(key.remove("DATE"));
        LocalDate createdDate = cacheConfig.getCreatedDate();
        int daysBetween = (int) ChronoUnit.DAYS.between(createdDate, localDate);

        /*
        * TODO: Maybe force non-negative, since date is pre creation date?
        * This makes the assumption that we are using the cache for exploring olden data, thus changing the creation date of the cache.
        * */
        if(daysBetween < 0){
            cacheConfig.setCreatedDate(localDate);
            daysBetween = 0;
        }

        //TODO: SPLIT FIELDS EQUAL TO LEVELS

        if(hasKeysExpired(localDate)){
            int numberOfExpiredKeys = (int) ChronoUnit.DAYS.between(cacheConfig.getExpireDate(), localDate);
            adjust(key, numberOfExpiredKeys);
        }

        List<List<String>> frequencyLevels = cacheConfig.getFrequencyLevels();
        List<List<String>> topLevels = cacheConfig.getTopLevels();
        List<CacheEntry> cacheEntries = new ArrayList<>(frequencyLevels.size());

        for(final List level : frequencyLevels){
            TreeMap<String,String> k = (TreeMap<String, String>) key.clone();
            k.keySet().retainAll(level);
            int pointFrequency = cms.put(k, daysBetween, amount); //cmr put
            cmr.put(k, daysBetween, amount); //cms put
            k.put("DATE", localDate.toString());
            cacheEntries.add(new CacheEntry(k, pointFrequency));
        }
        for(final List level : topLevels){
            TreeMap<String,String> k = (TreeMap<String, String>) key.clone();
            k.keySet().retainAll(level);
            String name = String.join("&", level);
            SlidingWindowTopList swt = swts.get(name);
            swt.put(k, daysBetween);
        }

        return cacheEntries;

    }
    public CacheEntry get(TreeMap<String,String> key){
        LocalDate localDate = LocalDate.parse(key.remove("DATE"));
        LocalDate createdDate = cacheConfig.getCreatedDate();
        int daysBetween = (int) ChronoUnit.DAYS.between(createdDate, localDate);

        return new CacheEntry(key, cms.get(key, daysBetween));
    }
    /*
    * Time stuff
    * Should probably be move to seaprate class
    * */
    protected boolean hasKeysExpired(LocalDate localDate){
        return localDate.isEqual(cacheConfig.getExpireDate()) || localDate.isAfter(cacheConfig.getExpireDate());
    }
    protected boolean hasExpireTimeWrappedAround(int numberOfExpiredKeys){
        return numberOfExpiredKeys == cacheConfig.getExpireDays();
    }
    protected void adjust(Object key, int numberOfExpiredKeys){

        if(hasExpireTimeWrappedAround(numberOfExpiredKeys)){ //it is safe to reinitialize the whole structure
            logger.log(Level.INFO, String.format("Cache: %s has wrapped around its expire time. Safely re-initializing it.", getName()));
            initializeSketches();
            cacheConfig.setCreatedDate(LocalDate.now()); //Update expire date
        } else {
            logger.log(Level.INFO, String.format("Found %d expired key candidates in cache, attempting to remove them.", numberOfExpiredKeys + 1));
            for(int day = 0; day < numberOfExpiredKeys; day++){ //TODO: Increment created date?
                cms.remove(key, day); //VERY CHEAP OPERATION SO NP
                cmr.remove(key, day);
            }
        }
    }
    protected void initializeSketches() {
        final int width = 1 << 14;
        final int depth = 4;
        final int numberOfSketches = (int)Math.ceil(cacheConfig.getExpireDays() / Math.log(2));
        this.cms = new CountMinSketch(width, depth, new FNV());
        this.cmr = new CountMinRange(width, depth, numberOfSketches);

        List<List<String>> topLevels = cacheConfig.getTopLevels();
        this.swts = new HashMap<>(topLevels.size()); //seems to be very costly.
        for(List level : topLevels){
            String name = String.join("-", level);
            swts.put(name, new SlidingWindowTopList(CacheConfig.TOP_WINDOW, CacheConfig.TOP_ITEMS));
            swts.put(name, new SlidingWindowTopList(CacheConfig.TOP_WINDOW, CacheConfig.TOP_ITEMS));
        }
    }

    @Override
    public String getName() {
        return cacheConfig.getCacheName();
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
    } //TODO: override

    @Override
    public CacheConfig getCacheConfig() {
        return this.cacheConfig;
    }



}
