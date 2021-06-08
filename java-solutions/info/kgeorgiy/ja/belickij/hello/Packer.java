package info.kgeorgiy.ja.belickij.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Packer {

    public static String makeResponse(final DatagramPacket receivedDatagramPacket) {
        return "Hello, " + new String(receivedDatagramPacket.getData(),
                receivedDatagramPacket.getOffset(),
                receivedDatagramPacket.getLength(),
                StandardCharsets.UTF_8);
    }

    public static DatagramPacket makeResponsePacket(final DatagramSocket datagramSocket) throws SocketException {
        return new DatagramPacket(
                new byte[datagramSocket.getReceiveBufferSize()],
                datagramSocket.getReceiveBufferSize());

    }

    public static DatagramPacket makeRequestPacket(final String requestString, final SocketAddress inetSocketAddress) {
        return new DatagramPacket(
                requestString.getBytes(StandardCharsets.UTF_8),
                requestString.length(),
                inetSocketAddress);
    }

    public static String getAnswerString(final DatagramPacket responseDatagramPacket) {
        return new String(responseDatagramPacket.getData(),
                responseDatagramPacket.getOffset(),
                responseDatagramPacket.getLength(),
                StandardCharsets.UTF_8);
    }

    public static String makeRequestString(final String prefix, final int threadId, final int requestId) {
        return (prefix +
                threadId +
                "_" +
                requestId);
    }

    public static Selector getSelector() {
        Selector selector = null;
        try {
            selector = Selector.open();
        } catch (final IOException exception) {
            System.err.println("Error while opening selector");
        }

        return selector;
    }

    public static DatagramChannel createDatagramChannel(final SocketOption<Boolean> option,
                                                        final boolean value,
                                                        final Selector selector,
                                                        final int selectionKey,
                                                        final int port) throws IOException {
        final DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.setOption(option, value);
        datagramChannel.configureBlocking(false);
        datagramChannel.bind(new InetSocketAddress(port));
        datagramChannel.register(selector, selectionKey);
        return datagramChannel;
    }


    public static void fillList(final List<DatagramChannel> list,
                                final int count,
                                final SocketAddress inetSocketAddress,
                                final Selector selector,
                                final List<Integer> intList,
                                final int requests) {
        for (int i = 0; i < count; i++) {
            try {
                final DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
                datagramChannel.connect(inetSocketAddress);
                datagramChannel.register(selector, SelectionKey.OP_WRITE);
                list.add(datagramChannel);
            } catch (final IOException exception) {
                System.err.println("Error while binding DatagramChannel " + exception.getMessage());
            }
        }
        for (int i = 0; i < count; i++) {
            intList.add(requests);
        }
    }


    public static boolean checkInput(final String[] args, final int expected) {
        if (args == null || args.length != expected) {
            return false;
        } else {
            return Arrays.stream(args).noneMatch(Objects::isNull);
        }
    }

    public static void startClient(final HelloClient client, final String[] args) {
        try {
            client.run(
                    args[0],
                    Integer.parseInt(args[1]),
                    args[2],
                    Integer.parseInt(args[3]),
                    Integer.parseInt(args[4])
            );
        } catch (final NumberFormatException ignored) {
            throw new IllegalArgumentException("Error while parsing arguments to Integer");
        }
    }

    public static void startServer(final HelloServer server, final String[] args) {
        try {
            server.start(
                    Integer.parseInt(args[0]),
                    Integer.parseInt(args[1])
            );
        } catch (final NumberFormatException ignored) {
            throw new IllegalArgumentException("Can't parse input arguments to correct port and count of threads");
        }
    }

    public static boolean checkString(final String str, final String container) {
        return str.contains(container);
    }


    public static void close(final ExecutorService executorService, final long time) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(time, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(time, TimeUnit.MILLISECONDS))
                    System.err.println("Error while terminating pool");
            }
        } catch (final InterruptedException ignored) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void get(final Map<SelectionKey, ByteBuffer> map,
                           final Map<SelectionKey, SocketAddress> sockets,
                           final SelectionKey key) throws IOException {
        final DatagramChannel channel = (DatagramChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.put("Hello, ".getBytes(StandardCharsets.UTF_8));
        try {
            final SocketAddress address = channel.receive(buffer);
            buffer.flip();
            map.put(key, buffer);
            sockets.put(key, address);
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (final IOException exception) {
            channel.close();
        }
    }

    public static void respond(final Map<SelectionKey, ByteBuffer> map,
                               final Map<SelectionKey, SocketAddress> sockets,
                               final SelectionKey key) throws IOException {
        final DatagramChannel channel = (DatagramChannel) key.channel();
        try {
            channel.send(map.get(key),sockets.get(key));
            key.interestOps(SelectionKey.OP_READ);
        } catch (final IOException exception) {
            System.err.println("Error while writing to channel");
            channel.close();
        }
    }


}
