package caching;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by heka1203 on 2017-04-05.
 */

public class CacheEntry<T, Integer> {
    private T key;
    private Integer value;

    @JsonCreator
    public CacheEntry(@JsonProperty(value="key", required = true)T key, @JsonProperty(value="value", required = true)Integer value){
        this.key = key;
        this.value = value;
    }

    //@Override
    public T getKey() {
        return this.key;
    }

    //@Override
    public Integer getValue() {
        return this.value;
    }

   // @Override
    public Integer setValue(Integer value) {
        this.value = value;
        return value;
    }

}
