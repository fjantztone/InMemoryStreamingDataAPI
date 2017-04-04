package sketching;

import com.google.gson.Gson;
import utils.JsonUtil;

import java.util.List;
import java.util.Objects;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfig {
    private String cacheName;
    private List<CacheField> cacheFields;
    private Query topQuery;
    private Query frequencyQuery;

    public boolean isNull(){
        return  Objects.isNull(getCacheName()) || Objects.isNull(getCacheFields())|| Objects.isNull(getTopQuery()) || Objects.isNull(getFrequencyQuery());
    }

    public String getCacheName() {
        return cacheName;
    }

    public List<CacheField> getCacheFields() {
        return cacheFields;
    }

    public Query getTopQuery() {
        return topQuery;
    }

    public Query getFrequencyQuery() {
        return frequencyQuery;
    }

    public String toString(){
        return JsonUtil.toJson(this);
    }

}
