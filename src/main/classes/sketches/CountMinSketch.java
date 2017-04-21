package sketches;

import hashing.FNV;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Created by heka1203 on 2017-04-01.
 */
public class CountMinSketch {
    private AtomicIntegerArray register;
    private int width;
    private final int depth;
    private final FNV fnv;

    /*private static class Emphasis{
        static final double BASE = 1.00;
        static int pre(int time, int amount){return (int)Math.round(Math.pow(BASE, time));}
        static int de(int time, int count){return (int)Math.round(count / Math.pow(BASE, time));}
    }*/

    public CountMinSketch(int width, int depth, FNV fnv){
        this.width = width;
        this.depth = depth;
        this.register = new AtomicIntegerArray(width*depth);
        this.fnv = fnv;
    }

    public int get(Object key, int days) {
        return put(key, days, 0);
    }

    public int remove(Object key, int days){
        int frequency = get(key, days);
        put(key, days, -frequency);
        return frequency;
    }


    public int put(Object key, int days, int amount){
        int min = Integer.MAX_VALUE;

        fnv.set(key.toString() + String.valueOf(days));

        for(int i = 0; i != depth; i++){
            int hash = fnv.next() % width;
            if(hash < 0)
                hash = ~hash;

            int index = i*width + hash;
            int value = register.addAndGet(index, amount);

            if(value < min)
                min = value;

        }
        return min;
    }

}
