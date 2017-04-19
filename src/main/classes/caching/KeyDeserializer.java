package caching;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Created by heka1203 on 2017-04-18.
 */
public class KeyDeserializer implements JsonDeserializer<Key> {
    @Override
    public Key deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json1, typeOfT1, context1) -> ZonedDateTime.parse(json1.getAsString()).toLocalDate()).create();
        Key key = gson.fromJson(json, Key.class);
        return key;
    }
}
