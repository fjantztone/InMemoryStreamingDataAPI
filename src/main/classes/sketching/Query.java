package sketching;

import java.util.List;

/**
 * Created by heka1203 on 2017-04-03.
 */
public class Query{
    private List<String> levels;
    private int window;

    public List<String> getLevels() {
        return levels;
    }

    public int getWindow() {
        return window;
    }

    public String toString(){
        return String.format("Levels: %s \t Window: %d", levels, window);
    }
}