package caching;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by heka1203 on 2017-04-17.
 */
public class CacheRangeEntry extends CacheEntry {
    @JsonSerialize(using = CacheDateSerializer.class)
    public LocalDateTime startDate;
    @JsonSerialize(using = CacheDateSerializer.class)
    public LocalDateTime endDate;

    public CacheRangeEntry(Object key, Integer value, LocalDateTime startDate, LocalDateTime endDate) {
        super(key, value);
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
