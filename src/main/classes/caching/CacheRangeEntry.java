package caching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by heka1203 on 2017-04-17.
 */
public class CacheRangeEntry extends CacheEntry {
    @JsonSerialize(using = CacheDateSerializer.class)
    @JsonDeserialize(using = CacheDateDeserializer.class)
    public LocalDateTime startDate;
    @JsonSerialize(using = CacheDateSerializer.class)
    @JsonDeserialize(using = CacheDateDeserializer.class)
    public LocalDateTime endDate;
    @JsonCreator
    public CacheRangeEntry(@JsonProperty("key") Object key, @JsonProperty("value") Integer value, @JsonProperty("startDate") LocalDateTime startDate, @JsonProperty("endDate") LocalDateTime endDate) {
        super(key, value);
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
