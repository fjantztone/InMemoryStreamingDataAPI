package caching;

import common.Validatable;
import utils.JsonUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfig implements Validatable {
    private String name;
    private int expireDays; // days
    private static int MAX_EXPIRE_DAYS = 31;
    private static int MIN_EXPIRE_DAYS = 1;
    public static int TOP_WINDOW = 7; //Non static for client side?
    public static int TOP_ITEMS = 5;
    private List<String> attributes;
    private List<List<String>> levels;
    private LocalDateTime createdAt;

    public void validate(){
        if(getName() == null) throw new IllegalStateException("Cache name is required.");
        if(getAttributes() == null) throw new IllegalStateException("Fields are required.");
        if(getLevels() == null) throw new IllegalStateException("Levels are required.");
        if(expireDays > MAX_EXPIRE_DAYS || expireDays < MIN_EXPIRE_DAYS) throw new IllegalStateException(String.format("Expire days field must exist and be between %d and %d", MIN_EXPIRE_DAYS, MAX_EXPIRE_DAYS));
    }

    public String getName() {
        return name;
    }
    public int getExpireDays(){return expireDays;}

    public List<String> getAttributes() {
        return attributes;
    }

    public List<List<String>> getLevels() {
        return levels;
    }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public LocalDateTime getExpireDate(){ return createdAt.plusDays(getExpireDays()); }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt = createdAt;
    }

    public String toString(){
        return JsonUtil.toJson(this);
    }

}
