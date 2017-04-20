package caching;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-18.
 */
public class Key {
    private LocalDateTime createdAt;
    private TreeMap<String, String> key;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TreeMap<String, String> getKey() {
        return key;
    }


}
