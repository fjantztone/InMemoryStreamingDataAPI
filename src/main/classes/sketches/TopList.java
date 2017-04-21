package sketches;

import caching.CacheEntry;
import caching.CachePointEntry;
import hashing.FNV;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-08.
 */
public class TopList{
    public CountMinSketch sketch;
    public FixedSizeNoDuplicatesPriorityQueue<TreeMap<String, String>> heap;
    private final int dayResidual;

    public TopList(int numberOfItems, int dayResidual){
        this.sketch = new CountMinSketch(1 << 12, 4, new FNV());
        this.dayResidual = dayResidual;
        heap = new FixedSizeNoDuplicatesPriorityQueue<>(numberOfItems, new SketchComparator(sketch, dayResidual));
    }
    public void put(TreeMap<String,String> key, int amount){
        sketch.put(key, dayResidual, amount);
        heap.add(key);
    }
    public List<CacheEntry> toCacheEntries(){

        PriorityQueue<TreeMap<String, String>> copy = new PriorityQueue<TreeMap<String, String>>(heap);
        List<CacheEntry> entries = new ArrayList<>(copy.size());
        LocalDate localDate = LocalDate.now().minusDays(dayResidual);

        while(!copy.isEmpty()){
            TreeMap<String,String> key = copy.poll();
            int frequency = sketch.get(key, dayResidual);
            entries.add(new CacheEntry(key, frequency));
        }
        return entries;
    }
}