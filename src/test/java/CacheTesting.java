/**
 * Created by heka1203 on 2017-04-06.
 */
import caching.CacheEntry;
import caching.CacheRangeEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoIterable;
import common.ResponseError;

import static com.sun.tools.internal.ws.wsdl.parser.Util.fail;

import org.bson.Document;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;
import spark.utils.IOUtils;
import utils.JsonUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CacheTesting {
    public static final String CACHE_NAME = "tests";
    public static final String BASE_URL = "http://localhost:8081/api/cache/" + CACHE_NAME;
    public static Logger logger = Logger.getLogger(CacheTesting.class.getName());
    public static final int WIDTH = 1 << 12;
    public static final int N = 1 << 2;
        /*
        * TOP 5
             2998_GPRS298288561GSM_REG_TJANST |      82628
             7214_HB4288561GSM_REG_TJANST     |      77294
             9220_SPOT3288561GSM_REG_TJANST   |      53032
             8506_SS4288561GSM_REG_TJANST     |      26103
             RK007380GSM_REG                  |      24941
        * */
    public static List<TreeMap<String,String>> TOP_KEYS = new ArrayList<TreeMap<String,String>>(){{
        add(new TreeMap<String,String>(){{put("ITEM", "2998_GPRS298"); put("RETAILER", "288561"); put("KOMMANDO", "GSM_REG_TJANST");}});
        add(new TreeMap<String,String>(){{put("ITEM", "7214_HB4"); put("RETAILER", "288561"); put("KOMMANDO", "GSM_REG_TJANST");}});
        add(new TreeMap<String,String>(){{put("ITEM", "9220_SPOT3"); put("RETAILER", "288561"); put("KOMMANDO", "GSM_REG_TJANST");}});
        add(new TreeMap<String,String>(){{put("ITEM", "8506_SS4"); put("RETAILER", "288561"); put("KOMMANDO", "GSM_REG_TJANST");}});
        add(new TreeMap<String,String>(){{put("ITEM", "RK"); put("RETAILER", "007380"); put("KOMMANDO", "GSM_REG");}});
    }};

    @BeforeClass
    public static void beforeClass() throws SQLException, ClassNotFoundException {
        Main.main(null);
        Spark.awaitInitialization();

    }

    /*@Test*/
    /*public void rangeQueryErrorTest() throws IOException { // url: /api/cache/oskar/filter/range/startdate/2017-04-21T20:14:02/enddate/2017-04-24T20:14:02/key/{"ITEM" : "qneher", "RETAILER" : "1234"}

        List<String> measurements = new ArrayList<>();
        measurements.add("range,mape,width");

        LocalDateTime start = LocalDateTime.parse("2017-01-01T10:10:00");
        ObjectMapper objectMapper = new ObjectMapper();
        for(int rangeSize = 0; rangeSize < 31; rangeSize++){
            LocalDateTime end = start.plusDays(rangeSize);
            int estimatedValue = 0;
            int trueValue = 0;
            for(TreeMap<String,String> key : TOP_KEYS){
                String jsonKey = objectMapper.writeValueAsString(key);
                String path = "/filter/range" + "/startdate/" + start.toString() + "/enddate/" + end.toString() + "/key/" + jsonKey;
                TestResponse testResponse = jsonRequest("GET", path, null);

                if(testResponse.status != 200){
                    ResponseError responseError = objectMapper.readValue(testResponse.body, ResponseError.class);
                    logger.info(responseError.getMessage());
                }
                else{
                    List<CacheRangeEntry> cacheRangeEntries = objectMapper.readValue(testResponse.body, new TypeReference<List<CacheRangeEntry>>(){});
                    estimatedValue += (int)cacheRangeEntries.get(0).getValue();
                    trueValue += MongoUtil.rangeQuery(key, start, end);
                }

            }
            final double mape = ((double)Math.abs(estimatedValue - trueValue) / trueValue) * 100; //MAPE in %
            measurements.add(String.format(Locale.US, "%d,%.2f,%d", rangeSize, mape, WIDTH));
        }
        Files.write(Paths.get(String.format("/Users/heka1203/Desktop/exjobb/measurements/range_error_w=%d.txt", WIDTH)), measurements);
        System.out.println(measurements);
    }*/

    //@Test
    public void webSocketTest(){

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new RequestTask());
        while(!executorService.isTerminated()){}
        System.out.println("Terminated");


    }
    class RequestTask implements Runnable{
        @Override
        public void run() {
            while(true){

                try {
                    Random random = new Random();
                    Thread.sleep(random.nextInt(150));
                    TreeMap<String,String> key = TOP_KEYS.get(random.nextInt(TOP_KEYS.size()));
                    ObjectMapper objectMapper = new ObjectMapper();
                    TestResponse testResponse = jsonRequest("POST", "", objectMapper.writeValueAsString(key));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /*@Test
    public void topQueryErrorTest() throws SQLException, IOException {
        LocalDate end = LocalDate.parse("2017-01-31");
        ObjectMapper objectMapper = new ObjectMapper();
            for(int days = 1; days <= 7; days++){
                LocalDate start = end.minusDays(days);
                String path = "/filter/top/days/" + days;
                TestResponse testResponse = jsonRequest("GET", path, null);
                if(testResponse.status != 200){
                    ResponseError responseError = objectMapper.readValue(testResponse.body, ResponseError.class);
                    logger.info(responseError.getMessage());
                }
                else{
                    List<CacheEntry> estimatedCacheEntries = objectMapper.readValue(testResponse.body, new TypeReference<List<CacheEntry>>(){});
                    List<CacheEntry> trueCacheEntries = SQLUtil.topQuery(start, end, N);

                    calculateCorrectlyEstimated(trueCacheEntries, estimatedCacheEntries);
                }

        }
    }*/
    @Test
    public void testTopQueryError() throws IOException {
        final int k = 32;
        LocalDateTime end = LocalDateTime.parse("2017-01-31T10:10:00");
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> measurements = new ArrayList<>();
        for(int day = 0; day < 7; day++){
            LocalDateTime start = end.minusDays(day);
            String path = String.format("/filter/top/days/%d", day+1);
            TestResponse testResponse = jsonRequest("GET", path, null);
            if(testResponse.status != 200){
                ResponseError responseError = objectMapper.readValue(testResponse.body, ResponseError.class);
                logger.info(responseError.getMessage());
            }
            else{
                List<CacheEntry> estimatedCacheEntries = objectMapper.readValue(testResponse.body, new TypeReference<List<CacheEntry<TreeMap<String,String>, Integer>>>(){});
                MongoIterable<Document> trueTopList = MongoUtil.topQuery(start, end, k);
                List<CacheEntry> trueCacheEntries = trueTopList.map(doc -> {
                    try {
                        String json = doc.toJson();
                        //dirty
                        json = json.replaceAll("_id", "key");
                        json = json.replaceAll("count", "value");
                        CacheEntry cacheEntry = objectMapper.readValue(json, new TypeReference<CacheEntry<TreeMap<String,String>, Integer>>(){});

                        return cacheEntry;
                    }  catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).into(new ArrayList<>());

                final double ce =calculateCorrectlyEstimated(trueCacheEntries, estimatedCacheEntries);
                String measurement = String.format(Locale.US, "%f,%d", ce, k);
                System.out.println(measurement);
                measurements.add(measurement);
                /*for(int i = 0; i < estimatedCacheEntries.size(); i++){
                    TreeMap<String,String> esimatedKey = (TreeMap<String,String>)estimatedCacheEntries.get(i).getKey();
                    TreeMap<String,String> trueKey = (TreeMap<String,String>)trueCacheEntries.get(i).getKey();
                    System.out.printf("%s\n", esimatedKey.toString().equals(trueKey.toString()));
                }
                System.out.println();
                System.out.println();*/
            }

        }
        Files.write(Paths.get(String.format("/Users/heka1203/Desktop/exjobb/measurements/top_error_k=%d.txt", k)), measurements);

    }
    public static double calculateCorrectlyEstimated(List<CacheEntry> trueCacheEntries, List<CacheEntry> estimatedCacheEntries){
        if(trueCacheEntries.size() != estimatedCacheEntries.size()) return 100f;
        double correct = 0f;
        double total = trueCacheEntries.size();

        List<String> trueKeys = trueCacheEntries.stream().map(cacheEntry -> cacheEntry.getKey().toString()).collect(Collectors.toList());
        List<String> estimatedKeys = estimatedCacheEntries.stream().map(cacheEntry -> cacheEntry.getKey().toString()).collect(Collectors.toList());
        for(int i = 0; i < trueKeys.size(); i++){
            String trueKey = trueKeys.get(i);
            String estimatedKey = estimatedKeys.get(i);
            if(trueKey.equals(estimatedKey)) correct += 1f;
            else if(trueKeys.contains(estimatedKey)) correct += 0.5f;
        }
        //System.out.printf("Total: %f, correct: %f\n", total, correct);
        return 100f * ((total-correct) / total);
    }

    private TestResponse jsonRequest(String method, String path, String json){
        try {
            URL url = new URL(BASE_URL + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setRequestMethod(method);
            if(method.equals("POST") && json != null){
                conn.setDoInput(true);
                try(OutputStream os = conn.getOutputStream()){
                    os.write(json.getBytes("UTF-8"));
                }
            }
            InputStream in;
            if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
                in = new BufferedInputStream(conn.getInputStream());
            } else {
                /* error from server */
                in = new BufferedInputStream(conn.getErrorStream());
            }
            String body = IOUtils.toString(in);
            in.close();
            conn.disconnect();
            return new TestResponse(conn.getResponseCode(), body);

        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            fail("Sending request failed: " + e.getMessage());
            return null;
        }
    }

    private static class TestResponse {

        public final String body;
        public final int status;

        public TestResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }
    }

}
