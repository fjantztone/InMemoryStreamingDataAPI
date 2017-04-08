package utils;

import com.google.gson.JsonParser;
import exceptions.FilterNotFoundException;
import exceptions.RequiresDateException;
import exceptions.RequiresValidDateException;
import caching.InputField;
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

/*
* TODO: This whole class can be simplified a lot if we were to allow only ISO formatted dates as input
*
*
* */
public class ParseUtil {

    public static TreeMap<String, String> parseJson(String json, List<InputField> jsonFields) throws RequiresValidDateException {
        TreeMap _json = JsonUtil.toSortedMap(new JsonParser().parse(json));
        _json.putIfAbsent("DATE", LocalDate.now().toString());

        for(InputField jsonField : jsonFields){
            Object o = _json.get(jsonField.getName());

            if((o instanceof String)){
                String fieldName = jsonField.getName();
                String fieldValue = (String)o;
                String regex = jsonField.getRegex();

                if(fieldName.equals("DATE") && !isValidISODate(fieldValue)){
                    throw new RequiresValidDateException("DATE is required to be in ISO-8000 format.");
                }
                if(!fieldValue.matches(regex)){
                    return null;
                }

            }
            else
                return null;
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
        for(InputField fileField: fileFields){
            String regex = fileField.getRegex();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(trimmedRow);
            if(matcher.find()){
                String fieldValue = "";
                String fieldName = fileField.getName();

                for(int c = 1; c <= matcher.groupCount(); c++)
                    if((fieldValue = matcher.group(c)) != null) break;

                if(fieldName.equals("DATE")){
                    fieldValue = formatFileDate(fieldValue); //TRY TO CONVERT DATE
                }
                json.put(fieldName, fieldValue);


            }
            else
               return null;

        }
        return json;
    }

    public static TreeMap<String, String> parseQueryParams(QueryParamsMap queryParams, String filter) throws RequiresValidDateException, FilterNotFoundException, RequiresDateException {

        TreeMap<String,String> json = new TreeMap<>();
        TreeMap<String,Object> queryMap = JsonUtil.toSortedMap(queryParams.toMap());
        //check filter
        //Convert map
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
        //TODO: rename filter
        switch(filter){
            case "point":
                if(isValidISODate(json.get("DATE"))){
                    break;
                }
                else
                    throw new RequiresDateException("A point query requires the DATE query parameter.");
            case "points":
            case "range":{
                if(isValidISODate(json.get("STARTDATE")) && isValidISODate(json.get("STARTDATE"))){
                    break;
                }
                else
                    throw new RequiresDateException(String.format("A %s query requires the STARTDATE & ENDDATE query parameters.", filter));
            }
            case "top":
                //Not implemented yet
                throw new UnsupportedOperationException("Top is not implemneted");
            default:
                throw new FilterNotFoundException(String.format("Query filter: %s does not exist. The availabe are: point, points, range & top.", filter));

        }

        return json;

    }
    //Om log eller json försök konvertera med substr?
    //Om query param
    protected static String formatFileDate(String date) throws RequiresValidDateException {

        if(isValidISODate(date)) return date;
        else{
            date = String.join("-", date.substring(0, 4), date.substring(4, 6), date.substring(6, 8)); // LOG SPECIFIC
            //unhandled nullpointer^
            if(isValidISODate(date)) return date;
            else throw new RequiresValidDateException("DATE could not be parsed, or converted to ISO-8000.."); //Not sure if to throw here..
        }
    }

    private static boolean isValidISODate(String date){
        try{
            LocalDate.parse(date);
            return true;
        }
        catch(DateTimeParseException e){
            return false;
        }
    }


}
