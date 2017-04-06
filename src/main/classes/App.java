/**
 * Created by heka1203 on 2017-03-31.
 */

import exceptions.RequiredDateException;

import static utils.JsonUtil.*;
import static spark.Spark.*;

public class App {
    CacheMiddleWare cacheMiddleWare = new CacheMiddleWare();
    public App(){

        path("/api", () -> {
            //Create CacheController

            path(Paths.CACHE, () -> {

                post("/", "application/json", (req, res) -> cacheMiddleWare.create(req.body()), json());
                put("/", "application/json", (req, res) -> cacheMiddleWare.edit(req.body()), json());
                post("/:name", "application/json", (req, res) -> cacheMiddleWare.putKey(req.body(), req.params(":name")), json());
                //post("/:name", "multipart/form-data", (req, res) -> cacheMiddleWare.putFile(req.raw().getPart("uploaded_file").getInputStream(), req.params(":name")), json());
                get("/:name/:filter", (req, res) -> cacheMiddleWare.getEntry(req.queryMap(), req.params(":name"), req.params(":filter")), json());

                after("/*", (req, res) -> {
                    res.type("application/json");
                });
                exception(Exception.class, (e, req, res) -> {
                    res.status(400);
                    res.type("application/json");
                    res.body(toJson(new ResponseError(e)));
                });

            });

        });

    }


}
