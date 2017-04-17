package services; /**
 * Created by heka1203 on 2017-03-31.
 */

import controllers.CacheController;
import com.google.gson.Gson;
import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.RequiresValidDateException;
import response.ResponseError;
import services.CacheService;

import static utils.JsonUtil.*;
import static spark.Spark.*;

public class AppService {
    public CacheService cacheService; //public for testing

    public AppService() throws RequiresValidDateException, CacheAlreadyExistsException, CacheNotFoundException {
        port(8081);
        before((req, res) -> {
            //CORS
            /*res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE");
            res.header("Access-Control-Allow-Headers: Origin, X-Requested-With", "Content-Type, Accept");*/
            res.type("application/json");
        });
        exception(Exception.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ResponseError(e)));
        });
        this.cacheService = new CacheService(new CacheController());

    }



}
