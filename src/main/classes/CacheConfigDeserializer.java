import com.google.gson.*;
import sketching.CacheConfig;

import java.lang.reflect.Type;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class CacheConfigDeserializer implements JsonDeserializer<CacheConfig> {
    @Override
    public CacheConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        CacheConfig cc = new Gson().fromJson(json, CacheConfig.class);
        if(cc.isNull()) throw new JsonParseException("The required fields are not present.");
        return cc;
    }
}
