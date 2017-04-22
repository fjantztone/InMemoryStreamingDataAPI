package subscription;

import caching.CacheEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by heka1203 on 2017-04-20.
 */
public class CacheEntryObservable implements Observable {

    private CacheEntry cacheEntry;
    private List<Observer> observers = new ArrayList<>();

    public CacheEntryObservable(CacheEntry cacheEntry){
        this.cacheEntry = cacheEntry;
    }

    public void setValue(int value){
        cacheEntry.setValue(value);
        notifyObserver();
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
