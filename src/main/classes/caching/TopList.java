package caching;

import hashing.FNV;

import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-08.
 */
public class TopList{
    public CountMinSketch sketch;
    public FixedSizeNoDuplicatesPriorityQueue<TreeMap<String, String>> heaps;
    private final int numberOfItems;
    private final int dayResidual;

    public TopList(int numberOfItems, int dayResidual){
        this.sketch = new CountMinSketch(1 << 8, 4, new FNV());
        this.numberOfItems = numberOfItems;
        this.dayResidual = dayResidual;
        heaps = new FixedSizeNoDuplicatesPriorityQueue<>(numberOfItems, new SketchComparator(sketch, dayResidual));
    }
    public void put(TreeMap<String,String> key, int amount){
        sketch.put(key, dayResidual, amount);
        heaps.add(key);
    }

}