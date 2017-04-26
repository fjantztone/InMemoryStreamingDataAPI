package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-21.
 */
public class SubscriberMessage {
    public static final String SUBSCRIBE_ACTION = "SUBSCRIBE";
    public static final String UNSUBSCRIBE_ACTION = "UNSUBSCRIBE";
    private String action;
    private List<TreeMap<String,String>> keys;

    @JsonCreator
    public SubscriberMessage(@JsonProperty(value="action", required = true)String action, @JsonProperty(value="keys", required = true)List<TreeMap<String,String>> keys){
        if(!action.equals(SUBSCRIBE_ACTION) && !action.equals(UNSUBSCRIBE_ACTION)) throw new IllegalArgumentException(String.format("Supported actions are %s & %s.", SUBSCRIBE_ACTION, UNSUBSCRIBE_ACTION));
        this.action = action;
        this.keys = keys;
    }
    public String getAction() {
        return action;
    }

    public List<TreeMap<String, String>> getKeys() {
        return keys;
    }
}
