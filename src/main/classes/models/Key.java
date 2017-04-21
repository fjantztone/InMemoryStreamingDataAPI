package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-18.
 */

public class Key {

    private TreeMap<String, String> key;
    private LocalDateTime createdAt;
    private LocalDateTime expireAt;

    @JsonCreator
    public Key(@JsonProperty(value = "key", required = true)TreeMap<String,String> key,
               @JsonProperty(value="createdAt", required = true)LocalDateTime createdAt,
               @JsonProperty(value="expireAt", required = true)LocalDateTime expireAt){
        this.key = key;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getExpireAt() { return expireAt; }
    public TreeMap<String, String> getKey() {
        return key;
    }


}
