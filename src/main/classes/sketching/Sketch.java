package sketching;

/**
 * Created by heka1203 on 2017-04-01.
 */
public interface Sketch {
    public int put(Object key, int amount);
    public int get(Object key);
}
