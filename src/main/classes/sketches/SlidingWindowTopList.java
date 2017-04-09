package sketches;

import caching.CacheEntry;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-08.
 */
/*
* TODO: EXAMINE ERROR ESTIMATE
*/
public class SlidingWindowTopList {
    private final int window;
    private int prevDayResidual = 0;
    private final int numberOfItems;
    private CircularFifoQueue<TopList> queue; //HEAPS

    public SlidingWindowTopList(int window, int numberOfItems){
        this.window = window;
        this.numberOfItems = numberOfItems;
        this.queue = new CircularFifoQueue(window);
        queue.add(new TopList(numberOfItems, prevDayResidual));
    }
    public void put(TreeMap<String,String> key, int day){

        int dayResidual = day % window;

        if(hasWindowPassed(dayResidual) || hasOneTimeUnitPassed(dayResidual)){
            queue.add(new TopList(numberOfItems, dayResidual));
            prevDayResidual = dayResidual;
        }
        queue.forEach(topList -> {
            topList.put(key, 1);
        });

    }
    public List<CacheEntry> toCacheEntries(int days){
        TopList topList = get(days);
        return topList.toCacheEntries();
    }
    protected TopList get(int days){
        dayRangeCheck(days);
        CircularFifoQueue<TopList> copy = new CircularFifoQueue(queue);
        TopList topList = null;
        for(int day = 0; day < window - (days - 1) && !copy.isEmpty(); topList = copy.poll(), days++){}
        return  topList;
    }
    protected void dayRangeCheck(int days){
        if (days > window || days <= 0)
            throw new IllegalArgumentException("Number of days must be between 1 and "+window+".");
    }
    protected boolean hasWindowPassed(int dayResidual){
        return dayResidual == 0 && prevDayResidual > 0;
    }
    protected boolean hasOneTimeUnitPassed(int dayResidual){
        return dayResidual > prevDayResidual;
    }




}
