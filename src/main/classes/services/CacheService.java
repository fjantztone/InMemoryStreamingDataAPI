package services;

import controllers.CacheController;

import static spark.Spark.*;
import static spark.Spark.get;
import static spark.Spark.post;
import static utils.JsonUtil.json;
import static utils.JsonUtil.fromTreeMap;
import static utils.JsonUtil.fromCacheConfig;

/**
 * Created by heka1203 on 2017-04-17.
 */
public class CacheService {
    protected CacheController cacheController;
    public CacheService(CacheController cacheController){
        this.cacheController = cacheController;
        initializeRoutes();
    }
    protected void initializeRoutes(){

        path("/api", () -> {
            //TODO: Change name? This is actually the "CacheService"

            path(Paths.CACHE, () -> {

                post("", "application/json", (req, res) -> cacheController.create(fromCacheConfig(req.body())), json());
                put("", "application/json", (req, res) -> cacheController.edit(fromCacheConfig(req.body())));
                delete("/:name", (req, res) -> cacheController.delete(req.params(":name")), json());
                get("/:name", (req, res) -> cacheController.get(req.params(":name")), json());

                post("/:name", "application/json", (req, res) -> cacheController.putKey(req.params(":name"), fromTreeMap(req.body())), json());
                get("/:name/filter/point/date/:date/key/:key", (req, res) -> cacheController.getPointEntry(req.params(":name"), req.params(":date"), fromTreeMap(req.body())), json());
                get("/:name/filter/points/startdate/:startdate/enddate/:enddate/key/:key", (req, res) -> cacheController.getPointsEntry(req.params(":name"), req.params(":startdate"), req.params(":enddate"), fromTreeMap(req.params(":key"))), json());
                get("/:name/filter/range/startdate/:startdate/enddate/:enddate/key/:key", (req, res) -> cacheController.getRangeEntry(req.params(":name"), req.params(":startdate"), req.params(":enddate"), fromTreeMap(req.params(":key"))), json());
                get("/:name/filter/top/days/:days", (req, res) -> cacheController.getTopEntry(req.params(":name"), Integer.valueOf(req.params(":days"))), json());


            });

        });
    }
}
