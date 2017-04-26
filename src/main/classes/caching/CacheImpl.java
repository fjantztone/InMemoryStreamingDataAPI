package caching;

import exceptions.InvalidKeyException;
import hashing.FNV;
import models.CacheConfig;
import services.CacheWebSocketHandler;
import sketches.CountMinRange;
import sketches.CountMinSketch;
import sketches.SlidingWindowTopList;
import sketches.TopList;
import subscription.CacheEntryObservable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by heka1203 on 2017-04-01.
 */

public class CacheImpl implements Cache<CacheEntry>{
    private static Logger logger = Logger.getLogger(CacheImpl.class.getName());
    private CountMinSketch cms;
    private CountMinRange cmr;
    private SlidingWindowTopList swt;
    private CacheConfig cacheConfig;

    public CacheImpl(CacheConfig cacheConfig) {
        this.cacheConfig = Objects.requireNonNull(cacheConfig);
        initializeSketches();
    }

    public CacheEntry pointGet(TreeMap<String,String> key, LocalDateTime localDateTime) {

        return get(key, localDateTime);
    }
    public List<CacheEntry> pointsGet(TreeMap<String,String> key, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        List<CacheEntry> cacheEntries = new ArrayList<>();
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateTime, endDateTime);
        for(int plusDays = 0; plusDays <= daysBetween; plusDays++){
            LocalDateTime current = startDateTime.plusDays(plusDays);
            cacheEntries.add(get(key, current));
        }
        return cacheEntries;

    }
    public List<CacheEntry> rangeGet(TreeMap<String,String> key, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        LocalDateTime createdDateTime = cacheConfig.getCreatedAt();//cache created date

        int start = (int) ChronoUnit.DAYS.between(createdDateTime, startDateTime);
        int end = (int) ChronoUnit.DAYS.between(createdDateTime, endDateTime);
        int rangeFrequency = cmr.get(key, start, end);

        return Arrays.asList(new CacheRangeEntry(key, rangeFrequency, startDateTime, endDateTime));

    }
    public List<CacheEntry> topGet(int days){
        TopList topList = swt.get(days);
        return topList.toCacheEntries();
    }
    @Override
    public List<CacheEntry> put(TreeMap<String,String> key, LocalDateTime localDateTime, int amount) throws InvalidKeyException {
        validateKey(key);
        LocalDateTime createdDateTime = cacheConfig.getCreatedAt();
        int daysBetween = (int) ChronoUnit.DAYS.between(createdDateTime, localDateTime);

        List<List<String>> levels = cacheConfig.getLevels();
        List<CacheEntry> cacheEntries = new ArrayList<>(levels.size());

        for(final List level : levels){
            @SuppressWarnings("unchecked")
            TreeMap<String,String> keyLevel = (TreeMap<String, String>) key.clone();
            keyLevel.keySet().retainAll(level);

            int pointFrequency = cms.put(keyLevel, daysBetween, amount); //cmr put
            cmr.put(keyLevel, daysBetween, amount); //cms put
            CachePointEntry cachePointEntry = new CachePointEntry(keyLevel, pointFrequency, localDateTime);
            cacheEntries.add(cachePointEntry);
            CacheWebSocketHandler.cacheEntryObservables.computeIfPresent(keyLevel, (k,c) -> {c.setValue(pointFrequency); return c;});

        }

        swt.put(key, daysBetween); //retain all in attributes

        return cacheEntries;

    }
    protected CacheEntry get(TreeMap<String,String> key, LocalDateTime localDateTime){
        LocalDateTime createdDateTime = cacheConfig.getCreatedAt();
        int daysBetween = (int) ChronoUnit.DAYS.between(createdDateTime, localDateTime);
        int pointFrequency = cms.get(key, daysBetween);
        return new CachePointEntry(key, pointFrequency, localDateTime);
    }

    /*
    * Time stuff
    * Should probably be moved to another place
    * */
    @Override
    public boolean hasExpired(LocalDateTime now){
        return now.isEqual(cacheConfig.getExpireAt()) || now.isAfter(cacheConfig.getExpireAt());
    }

    public void validateKey(TreeMap<String,String> key) throws InvalidKeyException { //May be a bad place
        if(key == null)
            throw new IllegalArgumentException("key == null");
        List<String> attributes = getCacheConfig().getAttributes();
        key.keySet().retainAll(attributes);
        if(key.size() != attributes.size())
            throw new InvalidKeyException(String.format("The provided key does not match the required attributes (%s).", attributes));
    }
    private void initializeSketches() {
        final int width = 1 << 14; //TODO: Make user defined, based on measurements?
        final int depth = 4;
        final int numberOfSketches = (int)Math.ceil(cacheConfig.getTimeToLive() / Math.log(2)); //argument to constructor
        this.cms = new CountMinSketch(width, depth, new FNV());
        this.cmr = new CountMinRange(width, depth, numberOfSketches);
        this.swt = new SlidingWindowTopList(7, 5); //TODO: user-defined
    }
    @Override
    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig;
    }

    @Override
    public CacheConfig getCacheConfig() {
        return this.cacheConfig;
    }



}
