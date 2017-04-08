package caching;

import java.util.LinkedList;

/**
 * Created by heka1203 on 2017-04-08.
 */
class FixedSizeFifoList<E> extends LinkedList<E> {
    final int maxSize;

    public FixedSizeFifoList(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean add(E e) {
        boolean added = super.add(e);
        while(size() > maxSize){
            super.removeLast();

        }
        return added; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public E get(int index) {
        if(index < 0 || index > maxSize - 1)
            throw new IndexOutOfBoundsException(String.format("Index must be between 1 and %d", maxSize-1));
        return super.get(index); //To change body of generated methods, choose Tools | Templates.
    }

}