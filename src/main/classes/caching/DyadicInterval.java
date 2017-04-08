package caching;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by heka1203 on 2017-04-05.
 */
public class DyadicInterval {
    public int level;
    public int start;

    public DyadicInterval(int level, int start){
        this.level = level;
        this.start = start;
    }

    public static List<DyadicInterval> createIntervals(int start, int end, int numberOfSketches){
        ArrayList<DyadicInterval> output = new ArrayList<>();
        Stack<Integer> left = new Stack<>();
        Stack<Integer> right = new Stack<>();

        left.push(Math.min(start, end));
        right.push(Math.max(start, end)+1);

        while(!left.empty()){
            int l = left.pop();
            int r = right.pop();

            for(int k = numberOfSketches; k >= 0; k--){
                int J = 1 << k;
                int L = (int)(J * Math.ceil((double)l / (double)J));
                int R = L + J - 1;

                if(R < r){
                    output.add(new DyadicInterval(k, L / J));
                    if(L > l){
                        left.push(l);
                        right.push(L);
                    }
                    if(R + 1 < r){
                        left.push(R + 1);
                        right.push(r);
                    }
                    break;

                }
            }
        }
        return output;
    }

}
