package utils;

import exceptions.RequiresValidDateException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;

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
    public static Date toDate(LocalDateTime localDateTime){ //Mongo uses java.util.Date..
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }
    public static LocalDateTime fromEpoch(long timeEpoch){
        Instant instant = Instant.ofEpochMilli(timeEpoch);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
