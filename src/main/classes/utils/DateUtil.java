package utils;

import exceptions.RequiresValidDateException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Created by heka1203 on 2017-04-20.
 */
public class DateUtil {
    public static void validateISODate(String date) throws RequiresValidDateException {
        try{
            LocalDateTime localDateTime = LocalDateTime.parse(date);
        }
        catch(DateTimeParseException e){
            throw new RequiresValidDateException("The date must be in ISO-8601 format, i.e (YYYY-MM-DDTHH:mm:ss).");
        }
    }
}
