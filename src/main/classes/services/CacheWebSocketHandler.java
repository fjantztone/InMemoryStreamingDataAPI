package services;
import caching.CacheEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.SubscriberMessage;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import subscription.CacheEntryObservable;
import subscription.Subscriber;
import static models.SubscriberMessage.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


@WebSocket
public class CacheWebSocketHandler {
    private static Logger logger = Logger.getLogger(CacheWebSocketHandler.class.getName());
    public static final ConcurrentHashMap<TreeMap<String,String>, CacheEntryObservable> cacheEntryObservables = new ConcurrentHashMap<>();

    @OnWebSocketConnect
    public void connected(Session session) {session.setIdleTimeout(1000 * 60 * 5);} //currently: drop after 5 min inactivity

    @OnWebSocketClose
    public void closed(Session session, int statusCode, String reason) {
        Subscriber subscriber = new Subscriber(session);
        for(CacheEntryObservable cacheEntryObservable : cacheEntryObservables.values()){
            cacheEntryObservable.removeObserver(subscriber);
        }
        logger.info(String.format("Client closed socket: (status: %d, reason: %s).", statusCode, reason));
    }

    @OnWebSocketMessage
    public void message(Session session, String message) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            SubscriberMessage subscriberMessage = objectMapper.readValue(message, SubscriberMessage.class);

            if(subscriberMessage.getAction().equals(SUBSCRIBE_ACTION)){
                for(TreeMap<String,String> key : subscriberMessage.getKeys()){
                    Subscriber subscriber = new Subscriber(session);
                    CacheEntryObservable cacheEntryObservable = new CacheEntryObservable(new CacheEntry(key, 0));
                    cacheEntryObservables.computeIfAbsent(key, k -> cacheEntryObservable).addObserver(subscriber);
                }
            }
            if(subscriberMessage.getAction().equals(UNSUBSCRIBE_ACTION)){
                System.out.println("Unsub");
                for(TreeMap<String,String> key : subscriberMessage.getKeys()){
                    Subscriber subscriber = new Subscriber(session);
                    cacheEntryObservables.computeIfPresent(key, (k,c) -> c.removeObserver(subscriber) && c.isEmpty() ? null : c);
                }
                session.close(); //Questionable

            }


        }
        catch(IOException | IllegalArgumentException e){
            System.out.println("error");
            logger.info(e.getMessage());
            session.close(400, e.getMessage());
        }

    }
}
