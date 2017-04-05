package utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import sketching.CacheField;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by heka1203 on 2017-04-01.
 */
public class ParseUtil {
    public static final LocalDate APP_START_DATE = LocalDate.now();

    public static String parseJson(JsonObject json, List<CacheField> allowableCacheFields){

        if(json.size() != allowableCacheFields.size()) return null;
        JsonObject out = new JsonObject();
        for(CacheField cacheField : allowableCacheFields){
            JsonElement fieldElement = json.get(cacheField.getName());
            //Ordered by rules
            if(fieldElement != null && fieldElement.isJsonPrimitive() && fieldElement.getAsString().matches(cacheField.getJsonPattern())){
                out.addProperty(cacheField.getName(), fieldElement.getAsString());
            }
            else
                return null;

        }
        return json.toString();
    }
    //Log file use pattern
    public static String parseFileEntry(String raw, List<CacheField> allowableCacheFields){
        //clean string
        String trimmedRaw = raw.replaceAll("(\\s){1,}", ";");
        JsonObject json = new JsonObject();

        for(CacheField cacheField : allowableCacheFields){
            Pattern pattern = Pattern.compile(cacheField.getFilePattern());
            Matcher matcher = pattern.matcher(trimmedRaw);
            if(matcher.find()){
                String match = "";
                for(int c = 1; c <= matcher.groupCount(); c++)
                    if((match = matcher.group(c)) != null) break;

                try {
                    Method filterMethod = ParseUtil.class.getDeclaredMethod(cacheField.getName().toLowerCase() + "FileFilter", String.class); //Very log specific...
                    match = (String)filterMethod.invoke(null, match);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    //There are no filters for this tag, so ignore it, and add the match as it is.
                } finally{
                    json.addProperty(cacheField.getName(), match);
                }

            }
            else
                return null;
        }
        return json.toString();
    }

    private static String dateFileFilter(String date){
        return String.join("-", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8));

    }


}
