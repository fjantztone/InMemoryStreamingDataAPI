package response;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class ResponseText {
    private String message;

    public ResponseText(String message, String[] args){
        this.message = String.format(message, args);
    }
    public ResponseText(String message){
        this.message = message;
    }

    public String getMessage(){
        return this.message;
    }
}
