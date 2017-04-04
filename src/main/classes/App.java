/**
 * Created by heka1203 on 2017-03-31.
 */

import static utils.JsonUtil.json;
import static utils.JsonUtil.toJson;
import static spark.Spark.*;

public class App {
    public App(){
        path("/api", () -> {
            //Create CacheController
            path(Paths.CACHE, () -> {

                post("/", "application/json", (req, res) -> CacheService.createCache(req, res), json());
                put("/:name", (req, res) -> CacheService.editCache(req, res), json());
                post("/:name/tuple", "application/json", (req, res) -> CacheService.createTupleInCache(req, res), json());
                post("/:name/upload", "multipart/form-data", (req, res) -> CacheService.uploadTupleToCache(req, res), json());
                get("/:name/:filter/tuple", (req, res) -> "?");
                after("/*", (req, res) -> {
                    res.type("application/json");
                });
                exception(Exception.class, (e, req, res) -> {
                    res.status(400);
                    res.body(toJson(new ResponseError(e)));
                });

            });



        });




    }


}
