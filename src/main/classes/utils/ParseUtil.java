package utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import exceptions.RequiredDateException;
import sketching.InputField;
import spark.QueryParamsMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by heka1203 on 2017-04-01.
 */
public class ParseUtil {
    public static final LocalDate APP_START_DATE = LocalDate.now();

    public static TreeMap<String, String> parseJson(String json, List<InputField> jsonFields){
        TreeMap _json = JsonUtil.toSortedMap(new JsonParser().parse(json));
        _json.putIfAbsent("DATE", LocalDate.now().toString());

        for(InputField jsonField : jsonFields){
            Object o = _json.get(jsonField.getName());
            if(!(o instanceof String) || !((String) o).matches(jsonField.getRegex())){
                return null;
            }

        }

        return _json;
    }
    //Log file use pattern
    public static TreeMap<String, String> parseFileRow(String row, List<InputField> fileFields) {
        //clean string
        String trimmedRow = row.replaceAll("(\\s){1,}", ";");
        TreeMap<String,String> json = new TreeMap();
        json.put("DATE", LocalDate.now().toString());
        //^date is required
        for(InputField fileField: fileFields){
            Pattern pattern = Pattern.compile(fileField.getRegex());
            Matcher matcher = pattern.matcher(trimmedRow);
            if(matcher.find()){
                String fieldValue = "";
                for(int c = 1; c <= matcher.groupCount(); c++)
                    if((fieldValue = matcher.group(c)) != null) break;

                String fieldName = fileField.getName();
                if(fieldName.equals("DATE")){
                    String formattedDate = toFormattedDate(fieldValue);
                    if(isValidDate(formattedDate))
                        json.put(fileField.getName(), formattedDate); //TODO: Handle null date.
                }

            }
        }
        return json.size() == fileFields.size() ? json : null;
    }

    public static TreeMap<String, String> parseQueryParams(QueryParamsMap queryParams){

        TreeMap<String,String> json = new TreeMap<>();
        TreeMap<String,Object> sortedMap = JsonUtil.toSortedMap(queryParams.toMap());

        for(Map.Entry<String,Object> pair : sortedMap.entrySet()){
            if(pair.getValue() instanceof ArrayList){
                ArrayList<String> values = (ArrayList)pair.getValue();
                String key = pair.getKey();
                String value = values.get(0);
                json.put(pair.getKey(), value.replaceAll("\\[|\\]", "")); //An idiot has built the queryparamsmap
            }
            else
                return null;

        }

        return json;

    }

    private static String toFormattedDate(String unformattedDate) {
        String formattedDate = String.join("-", unformattedDate.substring(0, 4), unformattedDate.substring(4, 6), unformattedDate.substring(6, 8));
        return formattedDate;

    }

    private static boolean isValidDate(String date){
        try{
            LocalDate.parse(date);
            return true;
        }
        catch(DateTimeParseException e){
            return false;
        }
    }


}
