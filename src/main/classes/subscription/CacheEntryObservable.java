package subscription;

import caching.CacheEntry;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.IOException;


/**
 * Created by heka1203 on 2017-04-20.
 */
public class CacheEntryObservable implements Observable {

    private CacheEntry cacheEntry;
    private ConcurrentHashSet<Observer> observers = new ConcurrentHashSet<>();


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
    public void notifyObserver() {
        for(Observer observer : observers){
            try {
                synchronized (Observer.class){
                    observer.update(this.cacheEntry);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
