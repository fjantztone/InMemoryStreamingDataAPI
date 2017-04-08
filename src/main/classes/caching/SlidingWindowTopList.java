package caching;

import hashing.FNV;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-08.
 */
/*
* TODO: Split into two classes
*/
public class SlidingWindowTopList {
    private final int window;
    private int prevDayResidual = 0;
    private final int numberOfItems;
    //private CircularFifoQueue<FixedSizeNoDuplicatesPriorityQueue<> heaps; //HEAPS
    private FixedSizeFifoList<TopList> queue;

    public SlidingWindowTopList(int window, int numberOfItems){
        this.window = window;
        this.numberOfItems = numberOfItems;
        //this.heaps = new CircularFifoQueue(window);
        this.queue = new FixedSizeFifoList<TopList>(window);
        queue.add(new TopList(numberOfItems, prevDayResidual));
    }
    public void put(TreeMap<String,String> key, int day){

        int dayResidual = day % window;

        /*if(window > day){
            for(int i = 0; i < dayResidual; i++){
                cms.remove(key, i);
            }

        }*/


        if(dayResidual == 0 && prevDayResidual > 0 || dayResidual > prevDayResidual){
            System.out.println("Resetting");
            queue.add(new TopList(numberOfItems, dayResidual));
            prevDayResidual = dayResidual;
            printTopList();
        }
        queue.forEach(topList -> {
            topList.put(key, 1);
        });

    }
    public void printKey(){
        TreeMap<String,String> key = new TreeMap<>();
        key.put("ITEM", "2998_GPRS298");
        key.put("KOMMANDO", "GSM_REG_TJANST");
        key.put("RETAILER", "288561");
        System.out.println(cms.get(key, 9));
    }
    public void printTopList(){

        for(int i = 0; i < heaps.size(); i++){
            FixedSizeNoDuplicatesPriorityQueue<TreeMap<String,String>> q = heaps.get(i);

            final int day = i;
            System.out.println("TOP LIST AT DAY: " + 9);
            q.forEach(key -> {
                int frequency = cms.get(key, day);

                System.out.printf("KEY: %s \t FREQUENCY: %d\n", key, frequency);
            });

        }


    }



}
