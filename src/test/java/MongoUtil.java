import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import utils.DateUtil;
import utils.JsonUtil;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.TreeMap;

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
}
