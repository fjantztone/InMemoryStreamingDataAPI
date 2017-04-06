package exceptions;

/**
 * Created by heka1203 on 2017-04-06.
 */
public class CacheNotFoundException extends Exception{
    public CacheNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
    public CacheNotFoundException(String message){
        super(message);
    }
}
