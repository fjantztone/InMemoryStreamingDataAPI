package caching;

import java.time.LocalDate;

/**
 * Created by heka1203 on 2017-04-17.
 */
public class CacheRangeEntry extends CacheEntry {
    public LocalDate startDate;
    public LocalDate endDate;

    public CacheRangeEntry(Object key, Integer value, LocalDate startDate, LocalDate endDate) {
        super(key, value);
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
