package info.kgeorgiy.ja.belickij.arrayset;

import java.util.AbstractList;
import java.util.List;

public class DescendingList<T> extends AbstractList<T> {

    final private List<T> array;

    private boolean reverse = false;

    void descend() {
        reverse = !reverse;
    }

    DescendingList(List<T> list) {
        array = list;
    }

    @Override
    public T get(int index) {
        if (!reverse) {
            return array.get(index);
        }
        return array.get(size() - index - 1);
    }

    @Override
    public int size() {
        return array.size();
    }

    List<T> getAll(){
        return array;
    }

    boolean getDescend(){
        return reverse;
    }
}

