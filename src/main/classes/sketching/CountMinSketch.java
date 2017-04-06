package sketching;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hashing.FNV;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Created by heka1203 on 2017-04-01.
 */
public class CountMinSketch implements Sketch{
    private int register[];
    private int width;
    private final int depth;
    private final FNV fnv;
    public static final LocalDate APP_START_DATE = LocalDate.now();

    /*private static class Emphasis{
        static final double BASE = 1.00;
        static int pre(int time, int amount){return (int)Math.round(Math.pow(BASE, time));}
        static int de(int time, int count){return (int)Math.round(count / Math.pow(BASE, time));}
    }*/

    public CountMinSketch(int width, int depth, FNV fnv){
        this.width = width;
        this.depth = depth;
        this.register = new int[width*depth];
        this.fnv = fnv;
    }

    public int get(Object key) {
        return put(key, 0);
    }

    public synchronized int put(Object key, int amount){
        int min = Integer.MAX_VALUE;

        fnv.set(key.toString());

        for(int i = 0; i != depth; i++){
            int hash = (fnv.next() & 0x7fffffff) % width;

            register[i*width + hash] += amount/*Emphasis.pre(dateDiff, amount)*/;
            if(register[i*width + hash] < min)
                min = register[i*width + hash];
        }
        return min;
    }

}
