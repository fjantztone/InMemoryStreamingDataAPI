package sketching;

import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Created by heka1203 on 2017-04-05.
 */
public class CacheEntry implements Map.Entry<JsonObject,Integer> {
    JsonObject key;
    Integer value;

    public CacheEntry(JsonObject key, Integer value){
        this.key = key;
        this.value = value;
    }

    @Override
    public JsonObject getKey() {
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