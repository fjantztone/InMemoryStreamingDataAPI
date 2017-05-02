package subscription;

import caching.CacheEntry;
import caching.CacheTickEntry;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by heka1203 on 2017-04-20.
 */
public class CacheEntryObservable implements Observable {

    private CacheTickEntry cacheEntry;
    private List<Observer> observers = new ArrayList<>();

    public CacheEntryObservable(CacheTickEntry cacheEntry){
        this.cacheEntry = cacheEntry;
    }

    public void setValue(int value, LocalDateTime nextTick){
        LocalDateTime oldTick = cacheEntry.getTick();
        int diff = (int) ChronoUnit.SECONDS.between(oldTick, nextTick);
        if(diff > CacheTickEntry.TICK_LENGTH){
            notifyObserver();
            cacheEntry.setTick(nextTick);
        }
        cacheEntry.setValue(value);


    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public boolean removeObserver(Observer o) {
        return observers.remove(o);
    }

    @Override
    public boolean isEmpty() {
        return observers.isEmpty();
    }
    @Override
    public void notifyObserver() {
        for(Observer observer : observers){
            try {
                observer.update(this.cacheEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
