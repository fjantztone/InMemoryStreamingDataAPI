package caching;

import java.time.LocalDate;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-18.
 */
public class Key {
    private LocalDate createdAt;
    private TreeMap<String, String> key;

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public TreeMap<String, String> getKey() {
        return key;
    }


}
