package sketches;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * Created by heka1203 on 2017-04-08.
 */
public class SketchComparator implements Comparator<TreeMap<String,String>> {
    private CountMinSketch sketch;
    int dayResidual;

    public SketchComparator(CountMinSketch sketch, int dayResidual){
        this.sketch = sketch;
        this.dayResidual = dayResidual;
    }
    @Override
    public int compare(TreeMap<String,String> key1, TreeMap<String,String> key2) {

        int frequency1 = sketch.get(key1, dayResidual);
        int frequency2 = sketch.get(key2, dayResidual);
        return Integer.compare(frequency1, frequency2);
    }

}
