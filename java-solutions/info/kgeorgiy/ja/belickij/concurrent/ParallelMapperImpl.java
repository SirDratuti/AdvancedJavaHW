package info.kgeorgiy.ja.belickij.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public final class ParallelMapperImpl implements ParallelMapper {

    private final Queue<Problem> problemQueue = new ArrayDeque<>();
    private final List<Thread> threadList = new ArrayList<>();
    private final Scheduler scheduler = new Scheduler();

    private void addProblem(final Problem problem) {
        problemQueue.add(problem);
        problemQueue.notify();
    }

    private final Runnable threadBase = () -> {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Problem problem;
                synchronized (problemQueue) {
                    while (problemQueue.isEmpty()) {
                        problemQueue.wait();
                    }
                    problem = problemQueue.poll();
                }
                problem.begin();
                endTask(problem.getSchedulerIndex());
            }
        } catch (final InterruptedException ignored) {
        }
    };

    private void endTask(final int index) {
        synchronized (scheduler) {
            scheduler.resolve(index);
        }
        synchronized (problemQueue) {
            problemQueue.notify();
        }
    }

    public ParallelMapperImpl(final int threads) throws IllegalArgumentException {
        if (threads <= 0) {
            throw new IllegalArgumentException("Not enough threads to parallel task");
        }
        for (int i = 0; i < threads; i++) {
            generateThread().start();
        }
    }

    private Thread generateThread() {
        final Thread currentThread = new Thread(threadBase);
        threadList.add(currentThread);
        return currentThread;
    }

    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {

        final List<R> resultList = new ArrayList<>(Collections.nCopies(args.size(), null));
        final int currentIndex;

        synchronized (scheduler) {
            currentIndex = scheduler.getNewCount();
            scheduler.add(args.size());
        }
        synchronized (problemQueue) {
            for (int i = 0; i < args.size(); i++) {
                final int finalI = i;
                addProblem(new Problem(() -> resultList.set(finalI, f.apply(args.get(finalI))), currentIndex));
            }
        }

        return join(currentIndex, resultList);
    }

    private <R> List<R> join(final int schedulerIndex, final List<R> results) throws InterruptedException {

        synchronized (scheduler) {
            while (!scheduler.isDone(schedulerIndex)) {
                scheduler.wait();
            }
        }
        return results;
    }

    @Override
    public void close() {
        threadList.forEach(Thread::interrupt);

        threadList.forEach(it -> {
            try {
                it.join();
            } catch (final InterruptedException ignored) {
            }
        });
    }
}
