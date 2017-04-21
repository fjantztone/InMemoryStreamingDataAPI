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
        CacheConfig cc = new Gson().fromJson(json, CacheConfig.class);
        cc.validate();
        return cc;
    }
}
