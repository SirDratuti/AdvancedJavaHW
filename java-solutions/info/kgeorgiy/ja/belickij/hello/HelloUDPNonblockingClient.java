package info.kgeorgiy.ja.belickij.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static info.kgeorgiy.ja.belickij.hello.Packer.*;

public class HelloUDPNonblockingClient implements HelloClient {

    private Selector selector;
    private final List<DatagramChannel> channels = new ArrayList<>();
    private final List<Integer> countdown = new ArrayList<>();
    private final Queue<SelectionKey> readQueue = new ArrayDeque<>();
    private int ended = 0;
    private int threads;
    private int requests;
    private String prefix;

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        this.threads = threads;
        this.requests = requests;
        this.prefix = prefix;
        selector = getSelector();
        if (selector == null) {
            System.err.println("Can't use selector");
            return;
        }

        fillList(channels,
                threads,
                new InetSocketAddress(host, port),
                selector,
                countdown,
                requests);

        begin();
    }

    private void begin() {
        while (!Thread.interrupted()) {
            try {
                selector.select(300);
            } catch (final IOException exception) {
                System.err.println("Error while selecting " + exception.getMessage());
            }
            if (selector.selectedKeys().isEmpty()) {
                while (!readQueue.isEmpty()) {
                    SelectionKey selectionKey = readQueue.poll();
                    final int ind = findInChannels((DatagramChannel) selectionKey.channel());
                    final int numberRequest = requests - countdown.get(ind);
                    if (checkWriteExit(numberRequest, requests)) {
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    }
                }
            }
            for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                final SelectionKey key = i.next();
                try {
                    if (key.isReadable()) {
                        final DatagramChannel channel = (DatagramChannel) key.channel();
                        final int ind = findInChannels(channel);
                        final ByteBuffer received = ByteBuffer.allocate(100);
                        try {
                            channel.receive(received);
                        } catch (final IOException exception) {
                            System.err.println("Error while receiving " + exception.getMessage());
                        }
                        if (checkString(
                                new String(received.array(), StandardCharsets.UTF_8),
                                makeRequestString(prefix, ind, requests - countdown.get(ind)))) {

                            decrementCountdown(ind);
                            final int numberRequest = requests - countdown.get(ind);
                            if (checkWriteExit(numberRequest, requests)) {
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (checkReadExit()) {
                                closeChannels();
                                Thread.currentThread().interrupt();
                            }

                        } else {
                            key.interestOps(SelectionKey.OP_WRITE);
                        }

                    } else if (key.isWritable()) {
                        final DatagramChannel channel = (DatagramChannel) key.channel();
                        final int ind = findInChannels(channel);
                        final int numberRequest = requests - countdown.get(ind);
                        try {
                            channel.write(
                                    ByteBuffer.wrap(
                                            makeRequestString(prefix, ind, numberRequest)
                                                    .getBytes(StandardCharsets.UTF_8))
                            );
                            readQueue.add(key);
                            key.interestOps(SelectionKey.OP_READ);
                        } catch (final IOException exception) {
                            System.err.println("Error while sending " + exception.getMessage());
                        }
                    }
                } finally {
                    i.remove();
                }
            }
        }
    }

    private int findInChannels(final DatagramChannel datagramChannel) {
        return channels.indexOf(datagramChannel);
    }

    private void decrementCountdown(final int index) {
        final int count = countdown.get(index);
        if (count == 1) {
            ended++;
        }
        countdown.set(index, count - 1);
    }

    private boolean checkReadExit() {
        return ended == threads;
    }

    private boolean checkWriteExit(final int currentRequest, final int requests) {
        return currentRequest < requests;
    }

    private void closeChannels() {
        channels.forEach(it -> {
            try {
                if (it.isOpen()) {
                    it.socket().close();
                    it.close();
                }
            } catch (final IOException exception) {
                System.err.println("Error while closing channels " + exception.getMessage());
            }
        });
    }


    public static void main(final String[] args) {
        if (checkInput(args, 5)) {
            startClient(new HelloUDPNonblockingClient(), args);
        } else {
            throw new IllegalArgumentException("Illegal input arguments");
        }
    }
}
