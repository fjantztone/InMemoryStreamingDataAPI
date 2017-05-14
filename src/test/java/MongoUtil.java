import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import org.bson.Document;
import utils.DateUtil;
import utils.JsonUtil;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by heka1203 on 2017-04-27.
 */
public class MongoUtil {
    private static final String DATABASE_NAME = "streamingdata";
    private static final String CACHE_NAME = "tests";
    private static final String CACHEKEYS_COLLECTION_NAME = "cachekeys";
    private static final MongoDatabase mongoDatabase = new MongoClient().getDatabase(DATABASE_NAME);

    public static int rangeQuery(TreeMap<String,String> key, LocalDateTime start, LocalDateTime end) throws JsonProcessingException {
        Date s = DateUtil.toDate(start.minusHours(1)); //<-- ensure we are between createdAt
        Date e = DateUtil.toDate(end.plusHours(1)); //<-- ensure we are between createdAt
        int trueValue = (int)mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).count(new Document("cacheName", CACHE_NAME)
                .append("key.ITEM", key.get("ITEM"))
                .append("key.RETAILER", key.get("RETAILER"))
                .append("key.KOMMANDO", key.get("KOMMANDO"))
                .append("createdAt", new Document("$gte", s).append("$lte", e)));
        return trueValue;
    }
    public static MongoIterable<Document> topQuery(LocalDateTime start, LocalDateTime end, int k){
        Date s = DateUtil.toDate(start.minusHours(1)); //<-- ensure we are between createdAt
        Date e = DateUtil.toDate(end.plusHours(1)); //<-- ensure we are between createdAt
        Document match = new Document("$match", new Document("cacheName", CACHE_NAME)
                .append("createdAt", new Document("$gte", s).append("$lte", e)));
        Document sort = new Document("$sortByCount", "$key");
        Document limit = new Document("$limit", k);
        Document order = new Document("$sort", new Document("count", 1));
        MongoIterable<Document> result = mongoDatabase.getCollection(CACHEKEYS_COLLECTION_NAME).aggregate(Arrays.asList(match, sort, limit, order));
        return result;
    }
}
