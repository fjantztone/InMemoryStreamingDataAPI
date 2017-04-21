package subscription;


/**
 * Created by heka1203 on 2017-04-20.
 */
public interface Observable {
    public void addObserver(Observer o);
    public boolean removeObserver(Observer o);
    public void notifyObserver();
}
