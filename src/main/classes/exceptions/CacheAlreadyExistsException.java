package exceptions;

/**
 * Created by heka1203 on 2017-04-06.
 */
public class CacheAlreadyExistsException extends Exception{
    public CacheAlreadyExistsException(String message){
        super(message);
    }
}
