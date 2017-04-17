package caching;

import java.time.LocalDate;

/**
 * Created by heka1203 on 2017-04-17.
 */
public class CachePointEntry extends CacheEntry {

    public LocalDate date;

    public CachePointEntry(Object key, Integer value, LocalDate date) {
        super(key, value);
        this.date = date;
    }
}
