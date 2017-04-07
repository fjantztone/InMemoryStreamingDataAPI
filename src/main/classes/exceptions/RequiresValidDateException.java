package exceptions;

/**
 * Created by heka1203 on 2017-04-06.
 */
public class RequiresValidDateException extends Exception{
    public RequiresValidDateException(String message, Throwable cause){
        super(message, cause);
    }
    public RequiresValidDateException(String message){
        super(message);
    }
}
