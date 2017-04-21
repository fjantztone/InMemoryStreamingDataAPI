package caching;

import java.time.LocalDate;

/**
 * Created by heka1203 on 2017-04-17.
 */
public class CacheRangeEntry extends CacheEntry {
    public String startDate;
    public String endDate;

    public CacheRangeEntry(Object key, Integer value, String startDate, String endDate) {
        super(key, value);
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
