package info.kgeorgiy.ja.belickij.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class IterativeParallelism implements ListIP {

    private final ParallelMapper parallelMapper;

    private <T, U> U evaluate(int threads,
                              final List<? extends T> values,
                              final Function<? super Stream<? extends T>, U> compareFunc,
                              final Function<? super Stream<U>, U> reduceFunc) throws InterruptedException {

        if (parallelMapper == null) {
            threads = Math.min(threads, values.size());
            final List<U> threadsResults = new ArrayList<>(Collections.nCopies(threads, null));
            final List<Thread> threadList = new ArrayList<>();
            final List<Stream<? extends T>> parts = divide(values, threads);
            for (int i = 0; i < threads; i++) {
                final int finalI = i;
                Thread currentThread = new Thread(() ->
                        threadsResults.set(finalI,
                                compareFunc.apply(parts.get(finalI))));
                currentThread.start();
                threadList.add(currentThread);
            }

            join(threadList);
            return reduceFunc.apply(threadsResults.stream());
        } else {
            return reduceFunc.apply(parallelMapper.map(compareFunc,
                    divide(values,Math.min(threads, values.size()))).stream());
        }


    }

    private <T> List<Stream<? extends T>> divide(final List<? extends T> values, final int count) {
        final int average = values.size() / count;
        final int rest = values.size() % count;
        final List<Stream<? extends T>> parts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            parts.add((values.subList(average * i + Math.min(i, rest),
                    average * (i + 1) + Math.min(i, rest)
                            + (rest > i ? 1 : 0))
                    .stream()));
        }
        return parts;
    }

    private void join(List<Thread> threadList) throws InterruptedException {
        for (Thread thread : threadList) {
            thread.join();
        }
    }

    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    public IterativeParallelism(){
        this(null);
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return evaluate(threads, values, stream -> stream.map(Object::toString).collect(Collectors.joining()), stream -> stream.collect(Collectors.joining()));
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return evaluate(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()), stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return evaluate(threads, values, stream -> stream.map(f).collect(Collectors.toList()), stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return evaluate(threads, values, stream -> stream.max(comparator).orElse(null), stream -> stream.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return evaluate(threads, values, stream -> stream.anyMatch(predicate), stream -> stream.anyMatch(Boolean::booleanValue));
    }

}