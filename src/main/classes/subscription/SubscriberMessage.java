package subscription;

import common.Validatable;

import java.util.List;
import java.util.TreeMap;

public class SubscriberMessage implements Validatable {
    public String action;
    public List<TreeMap<String,String>> keys;

    public String getAction(){return action;}
    public List<TreeMap<String,String>> getKeys() {
        return keys;
    }

    @Override
    public void validate() {
        if(!action.equals("SUB") && !action.equals("UNSUB"))
            throw new IllegalStateException("Required field is action with value: SUB or UNSUB");
        if(keys == null)
            throw new IllegalStateException("keys == null");
    }
}
