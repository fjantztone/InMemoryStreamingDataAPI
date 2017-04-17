package utils;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import spark.ResponseTransformer;

import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class JsonUtil {
    public static TreeMap toMap(Object object){
        return new Gson().fromJson(object.toString(), TreeMap.class);
    }
    public static String toJson(Object object){
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return json;
    }

    public static ResponseTransformer json() {
        return JsonUtil::toJson;
    }
}
