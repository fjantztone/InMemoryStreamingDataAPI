package services; /**
 * Created by heka1203 on 2017-03-31.
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import controllers.CacheController;
import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.InvalidKeyException;
import exceptions.RequiresValidDateException;
import common.ResponseError;
import spark.Request;
import spark.Response;


import java.io.IOException;
import java.util.logging.Logger;

import static utils.JsonUtil.*;
import static spark.Spark.*;

public class AppService {
    public CacheService cacheService; //public for testing
    public static final Logger logger = Logger.getLogger(AppService.class.getName());

    public AppService() throws RequiresValidDateException, CacheAlreadyExistsException, CacheNotFoundException, InvalidKeyException, IOException {
        port(8081);
        webSocket("/live", CacheWebSocketHandler.class);
        before((req, res) -> {
            //CORS
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE");
            res.header("Access-Control-Allow-Headers: Origin, X-Requested-With", "Content-Type, Accept");
            res.type("application/json");
        });
        exception(Exception.class, (Exception e, Request req, Response res) -> {
            res.status(400);
            try {
                res.body(toJson(new ResponseError(e)));
            } catch (JsonProcessingException e2) {
                logger.info(e2.getMessage());
            }
        });
        this.cacheService = new CacheService(new CacheController());

    }



}
