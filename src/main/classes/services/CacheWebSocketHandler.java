package services;

import caching.CacheEntry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import subscription.CacheEntryObservable;
import subscription.Subscriber;
import subscription.SubscriberMessage;
import subscription.SubscriberMessageDeserializer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebSocket
public class CacheWebSocketHandler {
    private static Logger logger = Logger.getLogger(CacheWebSocketHandler.class.getName());
    public static final ConcurrentHashMap<TreeMap<String,String>, CacheEntryObservable> cacheEntryObservables = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void connected(Session session) {session.setIdleTimeout(1000 * 60 * 5);} //drop after 5 min

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        logger.log(Level.INFO, String.format("Client closed socket with reason %s", reason));
        for(CacheEntryObservable cacheEntryObservable : cacheEntryObservables.values()){
            cacheEntryObservable.removeObserver(new Subscriber(session));
        }

    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
        try{
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(SubscriberMessage.class, new SubscriberMessageDeserializer());

            Gson gson = gsonBuilder.create();
            SubscriberMessage subscriberMessage = gson.fromJson(message, SubscriberMessage.class);

            for(TreeMap<String,String> key : subscriberMessage.getKeys()){
                CacheEntryObservable cacheEntryObservable = cacheEntryObservables.get(key);
                if(cacheEntryObservable == null){
                    cacheEntryObservable = new CacheEntryObservable(new CacheEntry(key, 0));
                }
                cacheEntryObservable.addObserver(new Subscriber(session));
                cacheEntryObservables.put(key, cacheEntryObservable);
            }

        }
        catch(JsonSyntaxException | IllegalStateException e){
            logger.log(Level.INFO, e.getMessage(), e);
            session.close(400, e.getMessage());
        }

    }
}
