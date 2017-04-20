package subscription;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by heka1203 on 2017-04-20.
 */
public class SubscriberMessageDeserializer implements JsonDeserializer<SubscriberMessage> {
    @Override
    public SubscriberMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        SubscriberMessage subscriberMessage = new Gson().fromJson(json, SubscriberMessage.class);
        subscriberMessage.validate();
        return subscriberMessage;
    }
}
