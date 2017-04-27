package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-18.
 */

public class Key {

    private TreeMap<String, String> key;
    private String cacheName;
    @JsonSerialize(using = MongoDateSerializer.class)
    @JsonDeserialize(using = MongoDateDeserializer.class)
    private LocalDateTime createdAt;
    @JsonCreator
    public Key(@JsonProperty(value = "key", required = true)TreeMap<String,String> key,
               @JsonProperty(value="cacheName", required = true)String cacheName,
               @JsonProperty(value="createdAt", required = true)LocalDateTime createdAt){
        this.key = key;
        this.cacheName = cacheName;
        this.createdAt = createdAt;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public TreeMap<String, String> getKey() {
        return key;
    }
    public String getCacheName(){return cacheName;}


}
