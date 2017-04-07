/**
 * Created by heka1203 on 2017-03-31.
 */

import exceptions.RequiresValidDateException;

import static utils.JsonUtil.*;
import static spark.Spark.*;

public class App {
    CacheMiddleWare cacheMiddleWare;
    public App(CacheMiddleWare cacheMiddleWare) throws RequiresValidDateException {
        this.cacheMiddleWare = cacheMiddleWare;
        port(9090);
        path("/api", () -> {
            //TODO: Change name? This is actually the "CacheController"

            path(Paths.CACHE, () -> {
                post("/", "application/json", (req, res) -> cacheMiddleWare.create(req, res), json());
                put("/", "application/json", (req, res) -> cacheMiddleWare.edit(req, res), json());
                post("/:name", "application/json", (req, res) -> cacheMiddleWare.putKey(req, res), json());
                post("/:name/upload", "multipart/form-data", (req, res) -> cacheMiddleWare.putFile(req, res), json());
                get("/:name/:filter", (req, res) -> cacheMiddleWare.getEntry(req, res), json());

            });

        });
        before((req, res) -> {

            /**
             *      CORS for testing
             **/

            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE");
            res.header("Access-Control-Allow-Headers: Origin, X-Requested-With", "Content-Type, Accept");
            res.type("application/json");
        });
        exception(Exception.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ResponseError(e)));
        });

    }


}
