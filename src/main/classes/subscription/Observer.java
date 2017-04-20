package subscription;

import caching.CacheEntry;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

/**
 * Created by heka1203 on 2017-04-20.
 */
public interface Observer {
    void update(Object object) throws IOException;
}
