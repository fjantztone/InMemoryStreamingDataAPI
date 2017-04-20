package caching;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfigDeserializer implements JsonDeserializer<CacheConfig> {
    @Override
    public CacheConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        //Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json1, typeOfT1, context1) -> ZonedDateTime.parse(json1.getAsString()).toLocalDateTime()).create();
        CacheConfig cc = new Gson().fromJson(json, CacheConfig.class);
        cc.validate();
        return cc;
    }
}
