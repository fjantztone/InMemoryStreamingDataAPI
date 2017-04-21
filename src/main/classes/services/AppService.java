package services; /**
 * Created by heka1203 on 2017-03-31.
 */

import controllers.CacheController;
import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.InvalidKeyException;
import exceptions.RequiresValidDateException;
import common.ResponseError;

import static utils.JsonUtil.*;
import static spark.Spark.*;

public class AppService {
    public CacheService cacheService; //public for testing

    public AppService() throws RequiresValidDateException, CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException {
        port(8081);
        webSocket("/cachestream", CacheWebSocketHandler.class);
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
