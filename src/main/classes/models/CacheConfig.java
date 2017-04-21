package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by heka1203 on 2017-04-21.
 */
@JsonIgnoreProperties({"_id", "expireAt"})
public class CacheConfig{
    private static int MAX_TIME_TO_LIVE = 31;
    private static int MIN_TIME_TO_LIVE = 1;

    private String name;
    private int timeToLive; // days
    private List<String> attributes;
    private List<List<String>> levels;
    @JsonSerialize(using = MongoDateSerializer.class)
    @JsonDeserialize(using = MongoDateDeserializer.class)
    private LocalDateTime createdAt = LocalDateTime.now();

    @JsonCreator
    public CacheConfig(@JsonProperty(value="name", required = true)String name,
                        @JsonProperty(value="timeToLive", required = true)int timeToLive,
                         @JsonProperty(value="attributes", required = true)List<String> attributes,
                          @JsonProperty(value="levels", required = true)List<List<String>> levels){
        this.name = name;
        if(timeToLive > MAX_TIME_TO_LIVE || timeToLive < MIN_TIME_TO_LIVE)
            throw new IllegalArgumentException(String.format("timeToLive must be between %d and %d days.", MIN_TIME_TO_LIVE, MAX_TIME_TO_LIVE));

        this.timeToLive = timeToLive;
        this.attributes = attributes;
        this.levels = levels;
    }

    public String getName() {
        return name;
    }

    public int getTimeToLive() {
        return timeToLive;
    }

    public List<String> getAttributes() {
        return attributes;
    }

    public List<List<String>> getLevels() {
        return levels;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    @JsonIgnore
    public LocalDateTime getExpireAt(){ return getCreatedAt().plusDays(getTimeToLive()); }


}
