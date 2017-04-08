package sketching;

import utils.JsonUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfig implements Validatable {
    private String cacheName;
    private int expireDays; // days

    private List<InputField> fileFields;
    private List<InputField> jsonFields;
    private List<String> topLevels;
    private List<String> frequencyLevels;

    public final LocalDate createdDate = LocalDate.now();


    @Override
    public boolean isValid(){
        return  getCacheName() != null && getFileFields() != null && getJsonFields() != null && getTopLevels() != null && getFrequencyLevels() != null; //TODO: fix validation and error handling
    }

    public String getCacheName() {
        return cacheName;
    }
    public int getExpireDays(){return expireDays;}
    public List<InputField> getFileFields() {
        return fileFields;
    }
    public List<InputField> getJsonFields() {
        return jsonFields;
    }
    public List<String> getTopLevels() {
        return topLevels;
    }
    public List<String> getFrequencyLevels() {
        return frequencyLevels;
    }

    public LocalDate getCreatedDate(){ return createdDate; }
    public LocalDate getExpireDate(){ return createdDate.plusDays(getExpireDays()); }

    public String toString(){
        return JsonUtil.toJson(this);
    }

}
