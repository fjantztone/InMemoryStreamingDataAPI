package caching;

import utils.JsonUtil;

import java.time.LocalDate;
import java.util.List;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfig implements Validatable {
    private String cacheName;
    private int expireDays; // days
    private static int MAX_EXPIRE_DAYS = 365;
    private static int MIN_EXPIRE_DAYS = 1;
    public static int TOP_WINDOW = 7; //Non static for client side?
    public static int TOP_ITEMS = 5;

    private List<InputField> fileFields;
    private List<InputField> jsonFields;
    private List<List<String>> topLevels;
    private List<List<String>> frequencyLevels;

    private LocalDate createdDate = LocalDate.now();

    public void validate(){
        if(getCacheName() == null) throw new IllegalStateException("Cache name is required.");
        if(getFileFields() == null) throw new IllegalStateException("File fields are required.");
        if(getTopLevels() == null) throw new IllegalStateException("Top levels are required.");
        if(getJsonFields() == null) throw new IllegalStateException("Json fields are required.");
        if(getFrequencyLevels() == null) throw  new IllegalStateException("Frequency levels are required");
        if(expireDays > MAX_EXPIRE_DAYS || expireDays < MIN_EXPIRE_DAYS) throw new IllegalStateException(String.format("Expire days field must exist and be between %d and %d", MIN_EXPIRE_DAYS, MAX_EXPIRE_DAYS));
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
    public List<List<String>> getTopLevels() {
        return topLevels;
    }
    public List<List<String>> getFrequencyLevels() {
        return frequencyLevels;
    }

    public LocalDate getCreatedDate(){ return createdDate; }
    public LocalDate getExpireDate(){ return createdDate.plusDays(getExpireDays()); }
    public void setCreatedDate(LocalDate createdDate){
        this.createdDate = createdDate;
    }

    public String toString(){
        return JsonUtil.toJson(this);
    }

}
