package caching;

import exceptions.InvalidKeyException;
import hashing.FNV;
import sketches.CountMinRange;
import sketches.CountMinSketch;
import sketches.SlidingWindowTopList;
import sketches.TopList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */

//TODO: FIX NAMED TOPLIST IN CACHE CONFIG AND SEPARATE

public class NamedCache implements Cache<CacheEntry>{
    private static Logger logger = Logger.getLogger(NamedCache.class.getName());
    private CountMinSketch cms;
    private CountMinRange cmr;
    private SlidingWindowTopList swt;
    private CacheConfig cacheConfig;

    public NamedCache(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
        initializeSketches();
    }

    public CacheEntry pointGet(TreeMap<String,String> key, LocalDate localDate) {
        return get(key, localDate);
    }
    public List<CacheEntry> pointsGet(TreeMap<String,String> key, LocalDate startDate, LocalDate endDate) {

        List<CacheEntry> cacheEntries = new ArrayList<>();
        int daysBetween = (int) ChronoUnit.DAYS.between(startDate, endDate);
        for(int plusDays = 0; plusDays <= daysBetween; plusDays++){
            LocalDate current = startDate.plusDays(plusDays);
            cacheEntries.add(get(key, current));
        }
        return cacheEntries;

    }
    public CacheEntry rangeGet(TreeMap<String,String> key, LocalDate startDate, LocalDate endDate) {

        List<CacheEntry> cacheEntries = new ArrayList<>();
        LocalDate createdDate = cacheConfig.getCreatedAt(); //cache created date

        int start = (int) ChronoUnit.DAYS.between(createdDate, startDate);
        int end = (int) ChronoUnit.DAYS.between(createdDate, endDate);
        int rangeFrequency = cmr.get(key, start, end);

        return new CacheRangeEntry(key, rangeFrequency, startDate, endDate);

    }
    public List<CacheEntry> topGet(int days){
        TopList topList = swt.get(days);
        return topList.toCacheEntries();
    }
    @Override
    public List<CacheEntry> put(TreeMap<String,String> key, LocalDate localDate, int amount) throws InvalidKeyException {
        validateKey(key);
        LocalDate createdDate = cacheConfig.getCreatedAt();
        int daysBetween = (int) ChronoUnit.DAYS.between(createdDate, localDate);


        if(hasKeysExpired(localDate)){
            int numberOfExpiredKeys = (int) ChronoUnit.DAYS.between(cacheConfig.getExpireDate(), localDate) % cacheConfig.getExpireDays();
            adjust(key, numberOfExpiredKeys);
        }

        List<List<String>> levels = cacheConfig.getLevels();
        List<CacheEntry> cacheEntries = new ArrayList<>(levels.size());

        for(final List level : levels){
            TreeMap<String,String> keyLevel = (TreeMap<String, String>) key.clone();
            keyLevel.keySet().retainAll(level);

            int pointFrequency = cms.put(keyLevel, daysBetween, amount); //cmr put
            cmr.put(keyLevel, daysBetween, amount); //cms put
            cacheEntries.add(new CachePointEntry(keyLevel, pointFrequency, localDate));
        }

        swt.put(key, daysBetween); //retain all in attributes

        return cacheEntries;

    }
    protected CacheEntry get(TreeMap<String,String> key, LocalDate localDate){
        LocalDate createdDate = cacheConfig.getCreatedAt();
        int daysBetween = (int) ChronoUnit.DAYS.between(createdDate, localDate);
        int pointFrequency = cms.get(key, daysBetween);
        return new CachePointEntry(key, pointFrequency, localDate);
    }

    /*
    * Time stuff
    * Should probably be moved to another place
    * */
    protected boolean hasKeysExpired(LocalDate localDate){
        return localDate.isEqual(cacheConfig.getExpireDate()) || localDate.isAfter(cacheConfig.getExpireDate());
    }
    protected void adjust(Object key, int numberOfExpiredKeys){

        logger.log(Level.INFO, String.format("Found %d expired key candidates in cache, attempting to remove them.", numberOfExpiredKeys + 1));
        for(int day = 0; day < numberOfExpiredKeys; day++){
            cms.remove(key, day); //VERY CHEAP OPERATION SO NP
        }

    }
    public void validateKey(TreeMap<String,String> key) throws InvalidKeyException { //May be a bad place
        List<String> attributes = getCacheConfig().getAttributes();
        key.keySet().retainAll(attributes);
        if(key.size() != attributes.size())
            throw new InvalidKeyException(String.format("The provided key does not match the required attributes (%s).", attributes));
    }
    protected void initializeSketches() {
        final int width = 1 << 14;
        final int depth = 4;
        final int numberOfSketches = (int)Math.ceil(cacheConfig.getExpireDays() / Math.log(2));
        this.cms = new CountMinSketch(width, depth, new FNV());
        this.cmr = new CountMinRange(width, depth, numberOfSketches);
        this.swt = new SlidingWindowTopList(CacheConfig.TOP_WINDOW, CacheConfig.TOP_ITEMS);
    }
    @Override
    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
    }

    @Override
    public CacheConfig getCacheConfig() {
        return this.cacheConfig;
    }



}
