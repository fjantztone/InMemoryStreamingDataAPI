package services;

import caching.CacheEntry;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import subscription.CacheEntryObservable;
import subscription.Subscriber;
import subscription.SubscriberMessage;
import static utils.JsonUtil.fromSubscriberMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


@WebSocket
public class CacheWebSocketHandler {
    private static Logger logger = Logger.getLogger(CacheWebSocketHandler.class.getName());
    public static final ConcurrentHashMap<TreeMap<String,String>, CacheEntryObservable> cacheEntryObservables = new ConcurrentHashMap<>();
    public static final String SUBSCRIBE_ACTION = "SUB";
    public static final String UNSUBSCRIBE_ACTION = "UNSUB";

    @OnWebSocketConnect
    public void connected(Session session) {session.setIdleTimeout(1000 * 60 * 5);} //currently: drop after 5 min inactivity

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        for(CacheEntryObservable cacheEntryObservable : cacheEntryObservables.values()){
            cacheEntryObservable.removeObserver(new Subscriber(session));
        }
        logger.log(Level.INFO, String.format("Client closed socket: (status: %d, reason: %s).", statusCode, reason));
    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
        try{
            SubscriberMessage subscriberMessage = fromSubscriberMessage(message);

            if(subscriberMessage.getAction().equals(SUBSCRIBE_ACTION)){
                for(TreeMap<String,String> key : subscriberMessage.getKeys()){

                    CacheEntryObservable cacheEntryObservable = cacheEntryObservables.get(key);
                    if(cacheEntryObservable == null){
                        cacheEntryObservable = new CacheEntryObservable(new CacheEntry(key, 0));
                    }
                    cacheEntryObservable.addObserver(new Subscriber(session));
                    cacheEntryObservables.put(key, cacheEntryObservable);

                }
            }
            if(subscriberMessage.getAction().equals(UNSUBSCRIBE_ACTION)){
                for(TreeMap<String,String> key : subscriberMessage.getKeys()){

                    CacheEntryObservable cacheEntryObservable = cacheEntryObservables.get(key);
                    if(cacheEntryObservable != null){
                        cacheEntryObservable.removeObserver(new Subscriber(session));
                    }
                }
                session.close();

            }


        }
        catch(JsonSyntaxException | IllegalStateException e){
            logger.info(e.getMessage());
            session.close(400, e.getMessage());
        }

    }
}
