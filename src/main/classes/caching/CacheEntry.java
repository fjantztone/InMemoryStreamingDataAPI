package caching;

import java.util.Map;

/**
 * Created by heka1203 on 2017-04-05.
 */
public class CacheEntry<T, Integer> implements Map.Entry<T,Integer> {
    private T key;
    private Integer value;

    public CacheEntry(T key, Integer value){
        this.key = key;
        this.value = value;
    }

    @Override
    public T getKey() {
        return this.key;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public Integer setValue(Integer value) {
        this.value = value;
        return value;
    }

}
