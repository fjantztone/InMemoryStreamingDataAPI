package caching;

import java.time.LocalDate;

/**
 * Created by heka1203 on 2017-04-17.
 */
public class CachePointEntry extends CacheEntry {

    public String date;

    public CachePointEntry(Object key, Integer value, String date) {
        super(key, value);
        this.date = date;
    }
}
