package subscription;


import caching.CacheEntry;
import org.eclipse.jetty.websocket.api.Session;
import static utils.JsonUtil.toJson;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by heka1203 on 2017-04-20.
 */
public class Subscriber implements Observer{

    private Session session;

    public Subscriber(Session session){
        this.session = session;
    };
    @Override
    public void update(Object object) throws IOException {
        @SuppressWarnings("unchecked")
        CacheEntry cacheEntry = (CacheEntry)object;
        session.getRemote().sendString(toJson(cacheEntry));
    }
    @Override
    public boolean equals(Object other){
        if(other == null) return false;
        if(!(other instanceof Subscriber)) return false;
        Subscriber subscriber = (Subscriber) other;
        if(this == subscriber) return true;
        if(this.session == subscriber.session) return true;
        return false;
    }
    @Override
    public int hashCode(){
        return Objects.hash(session.getRemoteAddress());
    }
}
