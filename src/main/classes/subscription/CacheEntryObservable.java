package subscription;

import caching.CacheEntry;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Logger;


/**
 * Created by heka1203 on 2017-04-20.
 */
public class CacheEntryObservable implements Observable {
    private static Logger logger = Logger.getLogger(CacheEntryObservable.class.getName());
    private CacheEntry<TreeMap<String,String>, Integer> cacheEntry;
    private ConcurrentHashSet<Observer> observers = new ConcurrentHashSet<>();

    public CacheEntryObservable(CacheEntry cacheEntry){
        this.cacheEntry = cacheEntry;
    }

    public void setValue(int value){
        cacheEntry.setValue(value);
        notifyObserver();
    }
    public CacheEntry getCacheEntry(){
        return this.cacheEntry;
    }

    @Override
    public void addObserver(Observer o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(Observer o) {
        observers.remove(o);
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
