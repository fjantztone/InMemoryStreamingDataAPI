/**
 * Created by heka1203 on 2017-04-06.
 */
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.AfterClass;

import static com.sun.tools.internal.ws.wsdl.parser.Util.fail;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;
import spark.utils.IOUtils;
import utils.JsonUtil;
import utils.ParseUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CacheTesting {

    @BeforeClass
    public static void beforeClass() {
        Main.main(null);
        Spark.awaitInitialization();
    }

    @AfterClass
    public static void afterClass() {
        Spark.stop();
    }

    @Test
    public void putKey(){
        JsonObject obj = new JsonObject();
        obj.addProperty("ITEM", "7214_HB4");
        obj.addProperty("KOMMANDO", "GSM_REG_AB");
        obj.addProperty("RETAILER", "12345");
        TestResponse res = jsonDataRequest("POST", "/sales", new Gson().toJson(obj));


        Map<String, String> json = res.json();
        assertNotNull(json.get("key"));
        assertNotNull(json.get("value"));
    }
    @Test
    public void putKey2(){
        JsonObject obj = new JsonObject();
        obj.addProperty("ITEM", "7214_HB4");
        obj.addProperty("KOMMANDO", "GSM_REG_AB");
        obj.addProperty("RETAILER", "12345");
        TestResponse res = jsonDataRequest("POST", "/salesf", new Gson().toJson(obj));

        System.out.println(res.status);
        assertEquals(res.status, 400);
    }

    private TestResponse jsonDataRequest(String method, String path, String json){
        try {
            URL url = new URL("http://localhost:9090/api/cache" + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(1000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(method);

            try(OutputStream os = conn.getOutputStream()){
                os.write(json.getBytes("UTF-8"));
            }

            // read the response
            InputStream in = new BufferedInputStream(conn.getInputStream());
            String body = IOUtils.toString(in);
            in.close();
            conn.disconnect();
            return new TestResponse(conn.getResponseCode(), body);

        } catch (IOException e) {
            e.printStackTrace();
            fail("Sending request failed: " + e.getMessage());
            return null;
        }
    }

    private static class TestResponse {

        public final String body;
        public final int status;

        public TestResponse(int status, String body) {
            System.out.println(status);
            System.out.println(body);
            this.status = status;
            this.body = body;
        }

        public Map<String,String> json() {
            return new Gson().fromJson(body, HashMap.class);
        }
    }
}
