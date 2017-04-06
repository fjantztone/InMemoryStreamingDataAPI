package exceptions;

/**
 * Created by heka1203 on 2017-04-06.
 */
public class RequiredDateException extends Exception{
    public RequiredDateException(String message, Throwable cause){
        super(message, cause);
    }
    public RequiredDateException(String message){
        super(message);
    }
}
