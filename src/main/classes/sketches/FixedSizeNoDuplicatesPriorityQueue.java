package sketches;

import java.util.Comparator;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Created by heka1203 on 2017-04-08.
 */
public class FixedSizeNoDuplicatesPriorityQueue<E> extends PriorityQueue<E> {
    final int maxSize;
    public FixedSizeNoDuplicatesPriorityQueue(int maxSize, Comparator<? super E> comparator){
        super(maxSize + 1, comparator);
        this.maxSize = maxSize;
    }

    private void adjust(){
        while(size() > maxSize){
            Optional<E> min = this.stream().min(comparator());
            if(min.isPresent())
                remove(min.get());
        }
    }
    @Override
    public boolean add(E e){
        boolean isAdded = false;
        if(!this.contains(e)){
            isAdded = super.add(e);
            adjust();
        }


        return isAdded;
    }
}
