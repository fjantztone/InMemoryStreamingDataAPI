package sketches;

import hashing.FNV;

/**
 * Created by heka1203 on 2017-04-01.
 */
public class CountMinSketch {
    private int register[];
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
        this.register = new int[width*depth];
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


    public synchronized int put(Object key, int days, int amount){
        int min = Integer.MAX_VALUE;

        fnv.set(key.toString() + String.valueOf(days));

        for(int i = 0; i != depth; i++){
            int hash = (fnv.next() & 0x7fffffff) % width;

            register[i*width + hash] += amount/*Emphasis.pre(dateDiff, amount)*/;
            if(register[i*width + hash] < min)
                min = register[i*width + hash];
        }
        return min;
    }

}
