package caching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-17.
 */

public class CachePointEntry extends CacheEntry<TreeMap<String,String>, Integer> {
    @JsonSerialize(using = CacheDateSerializer.class)
    public LocalDateTime date;

    @JsonCreator
    public CachePointEntry(@JsonProperty(value="key", required = true)TreeMap<String,String> key, @JsonProperty(value="value", required = true)Integer value, @JsonProperty(value="date", required = true)LocalDateTime date) {
        super(key, value);
        this.date = date;
    }
}
