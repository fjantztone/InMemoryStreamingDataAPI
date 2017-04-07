package sketching;

import utils.JsonUtil;

import java.util.List;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfig implements Validatable {
    private String cacheName;
    private List<InputField> fileFields;
    private List<InputField> jsonFields;
    private Query topQuery;
    private Query frequencyQuery;

    @Override
    public boolean isValid(){
        return  getCacheName() != null && getFileFields() != null && getJsonFields() != null && getTopQuery() != null && getFrequencyQuery() != null;
    }

    public String getCacheName() {
        return cacheName;
    }

    public List<InputField> getFileFields() {
        return fileFields;
    }
    public List<InputField> getJsonFields() {
        return jsonFields;
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
