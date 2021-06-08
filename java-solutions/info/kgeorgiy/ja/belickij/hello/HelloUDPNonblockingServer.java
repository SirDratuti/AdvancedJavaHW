package info.kgeorgiy.ja.belickij.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.*;
import java.util.concurrent.*;

import static info.kgeorgiy.ja.belickij.hello.Packer.*;

public class HelloUDPNonblockingServer implements HelloServer {

    private Selector selector;
    private DatagramChannel datagramChannel;

    private final ExecutorService executor;
    private final Map<SelectionKey, ByteBuffer> map;
    private final Map<SelectionKey, SocketAddress> socketMap;

    public HelloUDPNonblockingServer() {
        executor = Executors.newSingleThreadExecutor();
        map = new HashMap<>();
        socketMap = new HashMap<>();
    }

    @Override
    public void start(final int port, final int threads) {
        try {
            selector = getSelector();
            datagramChannel = createDatagramChannel(StandardSocketOptions.SO_REUSEADDR, true, selector, SelectionKey.OP_READ, port);
            executor.submit(this::begin);
        } catch (final IOException e) {
            System.err.println("Error while creating resources" + e.getMessage());
        }
    }

    private void begin() {
        while (!Thread.interrupted()) {
            try {
                selector.select(300);
                for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator();
                     i.hasNext(); ) {
                    final SelectionKey key = i.next();
                    try {
                        if (key.isReadable()) {
                            get(map, socketMap, key);
                        } else if (key.isWritable()) {
                            respond(map, socketMap, key);
                        }

                    } catch (final IOException exception) {
                        System.err.println("Error while closing thread");
                    } finally {
                        i.remove();
                    }
                }
            } catch (final IOException e) {
                System.err.println("Error while selecting keys " + e.getMessage());
                break;
            }
        }
    }

    @Override
    public void close() {
        try {
            selector.close();
            datagramChannel.socket().close();
            datagramChannel.close();
        } catch (final IOException e) {
            System.err.println("Error while closing resources");
        }
        Packer.close(executor, 1000L);
    }


    public static void main(final String[] args) {
        if (checkInput(args, 2)) {
            startServer(new HelloUDPNonblockingServer(), args);
        } else {
            throw new IllegalArgumentException("Illegal input arguments");
        }
    }
}