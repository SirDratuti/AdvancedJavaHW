package info.kgeorgiy.ja.belickij.concurrent;

import java.util.ArrayList;
import java.util.List;

public final class Scheduler {

    private final List<Integer> problemList;

    public Scheduler(final List<Integer> problemList) {
        this.problemList = problemList;
    }

    public Scheduler() {
        this(new ArrayList<>());
    }

    public Integer get(final int index) {
        return problemList.get(index);
    }

    public boolean isDone(final int index) {
        return get(index) == 0;
    }

    public int getNewCount() {
        return problemList.size();
    }

    public void add(final int count) {
        problemList.add(count);
    }

    public synchronized void resolve(final int index) {
        problemList.set(index, problemList.get(index) - 1);
        if (problemList.get(index) == 0) {
            this.notify();
        }
    }
}
