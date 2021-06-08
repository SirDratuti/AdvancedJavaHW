package info.kgeorgiy.ja.belickij.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public final class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;

    private final Map<String, DownloadsQueue> urlQueue;


    public WebCrawler(final Downloader downloader,
                      final int downloaders,
                      final int extractors,
                      final int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        urlQueue = new ConcurrentHashMap<>();
    }

    private static class DownloadsQueue {
        private final Queue<Runnable> queue;
        private int count;

        public DownloadsQueue() {
            queue = new ArrayDeque<>();
            count = 0;
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }

        public synchronized void addTask(final Runnable runnable) {
            queue.add(runnable);
        }

        public synchronized Runnable getTask() {
            return queue.poll();
        }

        public synchronized int getCount() {
            return count;
        }

        public synchronized void decrementCount() {
            count--;
        }

        public synchronized void incrementCount() {
            count++;
        }
    }

    class Solver {
        private final String url;
        private final int depth;
        private final Phaser phaser;

        public Solver(final String url, final int depth) {
            this.url = url;
            this.depth = depth;
            phaser = new Phaser();
        }

        public Result run() {
            Set<String> downloaded = new ConcurrentSkipListSet<>();
            Map<String, IOException> failed = new ConcurrentHashMap<>();
            phaser.register();
            try {
                final String host = URLUtils.getHost(url);
                download(url, depth, host, downloaded, failed);
            } catch (final MalformedURLException e) {
                failed.put(url, e);
            }
            phaser.arriveAndAwaitAdvance();
            return combine(downloaded, failed);
        }

        private void download(final String url, final int depth, final String host, Set<String> downloaded, Map<String, IOException> failed) {

            if (!downloaded.add(url)) {
                return;
            }

            final Runnable download = () -> {
                try {
                    Document document = downloader.download(url);
                    phaser.register();
                    extractors.submit(() -> {
                        try {
                            if (depth > 1 || depth == 0) {
                                final List<String> links = document.extractLinks();
                                for (String link : links) {
                                    try {
                                        final String hostName = URLUtils.getHost(link);
                                        download(link, depth - 1, hostName, downloaded, failed);
                                    } catch (final MalformedURLException e) {
                                        synchronized (failed) {
                                            failed.put(url, e);
                                        }
                                    }
                                }
                            }
                        } catch (final IOException exception) {
                            synchronized (failed) {
                                failed.put(url, exception);
                            }
                        } finally {
                            phaser.arrive();
                        }
                    });

                } catch (IOException exception) {
                    failed.put(url, exception);
                }

                if (urlQueue.containsKey(host)) {
                    DownloadsQueue queue = urlQueue.get(host);
                    if (queue.isEmpty()) {
                        queue.decrementCount();
                    } else {
                        downloaders.submit(queue.getTask());
                    }
                    urlQueue.put(host, queue);
                }
                phaser.arrive();
            };


            phaser.register();

            DownloadsQueue queue = urlQueue.get(host);
            if (queue == null) {
                queue = new DownloadsQueue();
            }
            if (queue.getCount() < perHost) {
                queue.incrementCount();
                downloaders.submit(download);
            } else {
                queue.addTask(download);
            }
            urlQueue.put(host, queue);

        }

        private Result combine(final Set<String> downloaded, final Map<String, IOException> failed) {
            final Set<String> errors = failed.keySet();
            downloaded.removeAll(errors);
            return new Result(new ArrayList<>(downloaded), failed);
        }

    }

    @Override
    public Result download(final String url, final int depth) {
        final Solver solver = new Solver(url, depth);
        return solver.run();
    }

    @Override
    public void close() {
        executeService(downloaders);
        executeService(extractors);
    }

    /**
     * Closing ExecutorServices. Code from Oracle's documentation
     *
     * @see <a href=https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html>Oracle's example</a>
     */
    private void executeService(final ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS))
                    System.err.println("Error while terminating pool");
            }
        } catch (final InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static boolean checkArguments(final String[] args) {
        if (args.length != 5) {
            return false;
        }
        return Arrays.stream(args).noneMatch(Objects::isNull);
    }

    public static void main(final String[] args) {
        if (checkArguments(args)) {
            try (final Crawler crawler = new WebCrawler(
                    new CachingDownloader(),
                    Integer.parseInt(args[2]),
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4]))
            ) {
                crawler.download(args[0], Integer.parseInt(args[1]));
            } catch (final IOException exception) {
                System.err.println(exception.getMessage());
            }
        }
    }
}
