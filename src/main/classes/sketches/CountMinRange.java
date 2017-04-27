package sketches;

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
    public void put(Object key, int days, int amount){
        for(int i = 0; i < numberOfSketches; i++){
            int granularity = days / (1 << i);
            sketches.get(i).put(key, granularity, amount);
        }

    }
    public void remove(Object key, int days){
        for(int i = 0; i < numberOfSketches; i++)
            sketches.get(i).remove(key, days/ (1 << i));
    }

    public int get(Object key, int start, int end){
        int frequency = 0;
        for(DyadicInterval d : DyadicInterval.createIntervals(start, end, numberOfSketches)){
            frequency += sketches.get(d.level).get(key, d.start);
        }

        return frequency;
    }

}
