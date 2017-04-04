/**
 * Created by heka1203 on 2017-04-03.
 */
public class ResponseError {
    private String message;
    public ResponseError(String message, String[] args){
        this.message = String.format(message, args);
    }
    public ResponseError(Exception e){
        this.message = e.getMessage();
    }

    public String getMessage(){
        return this.message;
    }
}
