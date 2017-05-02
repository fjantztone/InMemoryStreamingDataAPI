package caching;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-05-01.
 */
public class CacheTickEntry extends CacheEntry<TreeMap<String,String>, Integer> {
    @JsonSerialize(using = CacheTickSerializer.class)
    private LocalDateTime tick;
    public static int TICK_LENGTH = 5; //seconds

    public CacheTickEntry(TreeMap<String, String> key, Integer value, LocalDateTime tick) {
        super(key, value);
        this.tick = tick;
    }
    public void setTick(LocalDateTime tick){
        this.tick = tick;
    }
    public LocalDateTime getTick(){
        return this.tick;
    }
}
