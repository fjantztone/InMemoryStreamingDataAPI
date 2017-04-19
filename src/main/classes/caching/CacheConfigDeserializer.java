package caching;

import com.google.gson.*;
import caching.CacheConfig;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfigDeserializer implements JsonDeserializer<CacheConfig> {
    @Override
    public CacheConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json1, typeOfT1, context1) -> ZonedDateTime.parse(json1.getAsString()).toLocalDate()).create();
        CacheConfig cc = gson.fromJson(json, CacheConfig.class);
        cc.validate();
        return cc;
    }
}
