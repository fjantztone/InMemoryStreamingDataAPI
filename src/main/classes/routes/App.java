package routes; /**
 * Created by heka1203 on 2017-03-31.
 */

import caching.CacheMiddleWare;
import exceptions.RequiresValidDateException;
import response.ResponseError;

import static utils.JsonUtil.*;
import static spark.Spark.*;

public class App {
    public CacheMiddleWare cacheMiddleWare; //public for testing

    public App(CacheMiddleWare cacheMiddleWare) throws RequiresValidDateException {
        this.cacheMiddleWare = cacheMiddleWare;
        port(8081);
        path("/api", () -> {
            //TODO: Change name? This is actually the "CacheController"

            path(Paths.CACHE, () -> {
                post("/", "application/json", (req, res) -> cacheMiddleWare.create(req.body()), json());
                put("/", "application/json", (req, res) -> cacheMiddleWare.edit(req.body()));
                delete("/:name", (req, res) -> cacheMiddleWare.delete(req.params(":name")), json());
                get("/:name", (req, res) -> cacheMiddleWare.get(req.params(":name")), json());
                post("/:name", "application/json", (req, res) -> cacheMiddleWare.putKey(req.params(":name"), req.body()), json());

                get("/:name/filter/point/date/:date/key/:key", (req, res) -> cacheMiddleWare.getPointEntry(req.params(":name"), req.params(":key"), req.params(":date")), json());
                get("/:name/filter/range/startdate/:startdate/enddate/:enddate/key/:key", (req, res) -> cacheMiddleWare.getRangeEntry(req.params(":name"), req.params(":startdate"), req.params(":enddate"), req.params(":key")), json());
                get("/:name/filter/top/days/:days", (req, res) -> cacheMiddleWare.getTopEntry(req.params(":name"), Integer.valueOf(req.params(":days"))), json());
            });

        });
        before((req, res) -> {

            /**
             *
             * CORS for testing
             **/

            /*res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "PUT, GET, POST, DELETE");
            res.header("Access-Control-Allow-Headers: Origin, X-Requested-With", "Content-Type, Accept");*/
            res.type("application/json");
        });
        exception(Exception.class, (e, req, res) -> {
            res.status(400);
            res.body(toJson(new ResponseError(e)));
        });

    }


}
