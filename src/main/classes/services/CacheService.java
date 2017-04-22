package services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import controllers.CacheController;
import exceptions.CacheAlreadyExistsException;
import exceptions.CacheNotFoundException;
import exceptions.InvalidKeyException;
import exceptions.RequiresValidDateException;
import models.CacheConfig;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static spark.Spark.*;
import static spark.Spark.get;
import static spark.Spark.post;
import static utils.JsonUtil.json;

/**
 * Created by heka1203 on 2017-04-17.
 */

public class CacheService {

    private CacheController cacheController;
    public CacheService(CacheController cacheController){
        this.cacheController = cacheController;
        initializeRoutes();
    }
    private void initializeRoutes(){

        path("/api", () -> {

            path(Paths.CACHE, () -> {

                ObjectMapper objectMapper = new ObjectMapper();

                post("", "application/json", (req, res) -> cacheController.create(objectMapper.readValue(req.body(), CacheConfig.class)), json());
                put("", "application/json", (req, res) -> cacheController.edit(objectMapper.readValue(req.body(), CacheConfig.class)), json());
                delete("/:name", (req, res) -> cacheController.delete(req.params(":name")), json());
                get("/:name", (req, res) -> cacheController.get(req.params(":name")), json());

                post("/:name", "application/json", (req, res) -> cacheController.putKey(req.params(":name"), objectMapper.readValue(req.body(), new TypeReference<TreeMap<String,String>>(){})), json());
                get("/:name/filter/point/date/:date/key/:key", (req, res) -> cacheController.getPointEntry(req.params(":name"), req.params(":date"), objectMapper.readValue(req.params(":key"), new TypeReference<TreeMap<String,String>>(){})), json());
                get("/:name/filter/points/startdate/:startdate/enddate/:enddate/key/:key", (req, res) -> cacheController.getPointsEntry(req.params(":name"), req.params(":startdate"), req.params(":enddate"), objectMapper.readValue(req.params(":key"), new TypeReference<TreeMap<String,String>>(){})), json());
                get("/:name/filter/range/startdate/:startdate/enddate/:enddate/key/:key", (req, res) -> cacheController.getRangeEntry(req.params(":name"), req.params(":startdate"), req.params(":enddate"), objectMapper.readValue(req.params(":key"), new TypeReference<TreeMap<String,String>>(){})), json());
                get("/:name/filter/top/days/:days", (req, res) -> cacheController.getTopEntry(req.params(":name"), Integer.valueOf(req.params(":days"))), json());


            });

        });
    }
}
