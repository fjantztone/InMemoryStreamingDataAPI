package sketching;

import hashing.FNV;

import java.util.ArrayList;

/**
 * Created by heka1203 on 2017-04-06.
 */
public class CountMinRange{
    private final ArrayList<CountMinSketch> sketches = new ArrayList<>();
    private final int numberOfSketches;

    public CountMinRange(int width, int depth, int numberOfSketches){
        this.numberOfSketches = numberOfSketches;
        for(int i = 0; i < this.numberOfSketches; i++)
            sketches.add(new CountMinSketch(width, depth, new FNV()));
    }
    public int put(Object key, int days, int amount){
        int frequency = 0;
        for(int i = 0; i < numberOfSketches; i++)
            frequency = sketches.get(i).put(key, days/ (1 << i), amount);
        return frequency;
    }

    public int get(Object key, int start, int end){
        int frequency = 0;
        for(DyadicInterval d : DyadicInterval.createIntervals(start, end, numberOfSketches)){
            frequency += sketches.get(d.level).get(key, d.start);
        }
        return frequency;
    }

}
