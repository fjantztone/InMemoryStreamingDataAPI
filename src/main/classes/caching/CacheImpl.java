package caching;

import exceptions.InvalidKeyException;
import hashing.FNV;
import models.CacheConfig;
import org.junit.Assert;
import services.CacheWebSocketHandler;
import sketches.CountMinRange;
import sketches.CountMinSketch;
import sketches.SlidingWindowTopList;
import sketches.TopList;
import subscription.CacheEntryObservable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Level;
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
        int daysBetween = (int) ChronoUnit.DAYS.between(startDateTime.toLocalDate(), endDateTime.toLocalDate());
        for(int plusDays = 0; plusDays <= daysBetween; plusDays++){
            LocalDateTime current = startDateTime.plusDays(plusDays);
            cacheEntries.add(get(key, current));
        }
        return cacheEntries;

    }
    public CacheEntry rangeGet(TreeMap<String,String> key, LocalDateTime startDateTime, LocalDateTime endDateTime) {

        LocalDateTime createdDateTime = cacheConfig.getCreatedAt();//cache created date

        int start = (int) ChronoUnit.DAYS.between(createdDateTime.toLocalDate(), startDateTime.toLocalDate());
        int end = (int) ChronoUnit.DAYS.between(createdDateTime.toLocalDate(), endDateTime.toLocalDate());
        int rangeFrequency = cmr.get(key, start, end);

        return new CacheRangeEntry(key, rangeFrequency, startDateTime.toLocalDate().toString(), endDateTime.toLocalDate().toString());

    }
    public List<CacheEntry> topGet(int days){
        TopList topList = swt.get(days);
        return topList.toCacheEntries();
    }
    @Override
    public List<CacheEntry> put(TreeMap<String,String> key, LocalDateTime localDateTime, int amount) throws InvalidKeyException {
        validateKey(key);
        LocalDateTime createdDateTime = cacheConfig.getCreatedAt();

        int daysBetween = (int) ChronoUnit.DAYS.between(createdDateTime.toLocalDate(), localDateTime.toLocalDate());


        if(hasKeysExpired(localDateTime)){ //TODO: update expire date in DB if wrapped?
            int numberOfExpiredKeys = (int) ChronoUnit.DAYS.between(cacheConfig.getExpireAt(), localDateTime) % cacheConfig.getExpireDays(); //<-- modifiable
            adjust(key, numberOfExpiredKeys);
        }

        List<List<String>> levels = cacheConfig.getLevels();
        List<CacheEntry> cacheEntries = new ArrayList<>(levels.size());

        for(final List level : levels){
            TreeMap<String,String> keyLevel = (TreeMap<String, String>) key.clone();
            keyLevel.keySet().retainAll(level);

            int pointFrequency = cms.put(keyLevel, daysBetween, amount); //cmr put
            cmr.put(keyLevel, daysBetween, amount); //cms put
            CachePointEntry cachePointEntry = new CachePointEntry(keyLevel, pointFrequency, localDateTime.toLocalDate().toString());
            cacheEntries.add(cachePointEntry);

            if(CacheWebSocketHandler.cacheEntryObservables.containsKey(keyLevel)){
                CacheEntryObservable cacheEntryObservable = CacheWebSocketHandler.cacheEntryObservables.get(keyLevel);
                synchronized (cacheEntryObservable){
                    cacheEntryObservable.setValue(pointFrequency);
                }

            }

        }

        swt.put(key, daysBetween); //retain all in attributes

        return cacheEntries;

    }
    protected CacheEntry get(TreeMap<String,String> key, LocalDateTime localDateTime){
        LocalDateTime createdDateTime = cacheConfig.getCreatedAt();
        int daysBetween = (int) ChronoUnit.DAYS.between(createdDateTime.toLocalDate(), localDateTime.toLocalDate());
        int pointFrequency = cms.get(key, daysBetween);
        return new CachePointEntry(key, pointFrequency, localDateTime.toLocalDate().toString());
    }

    /*
    * Time stuff
    * Should probably be moved to another place
    * */

    protected boolean hasKeysExpired(LocalDateTime localDateTime){
        return localDateTime.isEqual(cacheConfig.getExpireAt()) || localDateTime.isAfter(cacheConfig.getExpireAt());
    }
    protected void adjust(Object key, int numberOfExpiredKeys){

        logger.log(Level.INFO, String.format("Found %d expired key candidates in cache, attempting to remove them.", numberOfExpiredKeys + 1));
        for(int day = 0; day < numberOfExpiredKeys; day++){
            cms.remove(key, day); //VERY CHEAP OPERATION SO NP
        }

    }
    public void validateKey(TreeMap<String,String> key) throws InvalidKeyException { //May be a bad place
        if(key == null)
            throw new InvalidKeyException("key == null");
        List<String> attributes = getCacheConfig().getAttributes();
        key.keySet().retainAll(attributes);
        if(key.size() != attributes.size())
            throw new InvalidKeyException(String.format("The provided key does not match the required attributes (%s).", attributes));
    }
    private void initializeSketches() {
        final int width = 1 << 14; //TODO: Make user defined, based on measurements?
        final int depth = 4;
        final int numberOfSketches = (int)Math.ceil(cacheConfig.getExpireDays() / Math.log(2));
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
