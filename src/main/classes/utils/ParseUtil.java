package utils;

import com.google.gson.JsonParser;
import exceptions.FilterNotFoundException;
import exceptions.RequiresDateException;
import exceptions.RequiresValidDateException;
import sketching.InputField;
import spark.QueryParamsMap;

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

    public static TreeMap<String, String> parseJson(String json, List<InputField> jsonFields) throws RequiresValidDateException {
        TreeMap _json = JsonUtil.toSortedMap(new JsonParser().parse(json));
        Object date = _json.putIfAbsent("DATE", LocalDate.now().toString());
        if(date instanceof String){
            _json.put("DATE", formatDate((String)date)); //TODO: Maybe handle similar to parseFileRow?
        }
        for(InputField jsonField : jsonFields){
            Object o = _json.get(jsonField.getName());
            if(!(o instanceof String) || !((String) o).matches(jsonField.getRegex())){
                return null;
            }

        }

        return _json;
    }
    //Log file use pattern
    public static TreeMap<String, String> parseFileRow(String row, List<InputField> fileFields) throws RequiresValidDateException {
        //clean string
        String trimmedRow = row.replaceAll("(\\s){1,}", ";");
        TreeMap<String,String> json = new TreeMap();
        json.put("DATE", LocalDate.now().toString());
        //^date is default if not present
        System.out.println("Trying to parse: " + trimmedRow);
        for(InputField fileField: fileFields){
            String regex = fileField.getRegex();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(trimmedRow);
            if(matcher.find()){
                String fieldValue = "";
                for(int c = 1; c <= matcher.groupCount(); c++)
                    if((fieldValue = matcher.group(c)) != null) break;

                String fieldName = fileField.getName();
                if(fieldName.equals("DATE")){
                    fieldValue = formatDate(fieldValue);
                }
                json.put(fieldName, fieldValue);


            }
            else{
               return null;
            }
        }
        return json;
    }

    public static TreeMap<String, String> parseQueryParams(QueryParamsMap queryParams, String filter) throws RequiresValidDateException, FilterNotFoundException, RequiresDateException {

        TreeMap<String,String> json = new TreeMap<>();
        TreeMap<String,Object> queryMap = JsonUtil.toSortedMap(queryParams.toMap());
        //check filter

        //TODO: Handle the damn query map better
        for(Map.Entry<String,Object> pair : queryMap.entrySet()){
            if(pair.getValue() instanceof ArrayList){
                ArrayList<String> values = (ArrayList)pair.getValue();
                String key = pair.getKey();
                String value = values.get(0);
                json.put(pair.getKey(), value.replaceAll("\\[|\\]", "")); //An idiot has built the queryparamsmap
            }
            else
                return null;

        }
        if(filter.equals("point")){
            //DATE
            if(json.containsKey("DATE"))
                json.put("DATE", formatDate(json.get("DATE")));
            else
                throw new RequiresDateException("A point query requires the DATE query parameter.");


        }
        else if(filter.equals("points") || filter.equals("range")) {
            if(json.containsKey("STARTDATE") && json.containsKey("ENDDATE")){
                json.put("STARTDATE", formatDate(json.get("STARTDATE")));
                json.put("ENDDATE", formatDate(json.get("ENDDATE")));
            }
            else
                throw new RequiresDateException(String.format("A %s query requires the STARTDATE & ENDDATE query parameter.", filter));

        }

        else if(filter.equals("top")){
            //not implemented yet
            throw new UnsupportedOperationException("Top is not implemneted");
        }
        else
            throw new FilterNotFoundException(String.format("Query filter: %s does not exist. The availabe are: point, points, range & top.", filter));

        return json;

    }
    protected static String formatDate(String date) throws RequiresValidDateException {
        if(isValidDate(date)) return date;
        else{
            date = String.join("-", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8)); // LOG SPECIFIC
            if(isValidDate(date)) return date;
            throw new RequiresValidDateException("DATE could not be parsed, or converted to ISO-8000.."); //Not sure if to throw here..
        }
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
