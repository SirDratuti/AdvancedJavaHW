package info.kgeorgiy.ja.belickij.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

import static info.kgeorgiy.ja.belickij.hello.Packer.*;

public class HelloUDPServer implements HelloServer {

    private ExecutorService senders;
    private final ExecutorService receivers;
    private DatagramSocket datagramSocket;

    public static void main(final String[] args) {
        if (checkInput(args, 2)) {
            startServer(new HelloUDPServer(), args);
        } else {
            throw new IllegalArgumentException("Illegal input arguments");
        }
    }

    public HelloUDPServer() {
        receivers = Executors.newSingleThreadExecutor();
    }

    @Override
    public void start(final int port, final int threads) {

        try {
            senders = Executors.newFixedThreadPool(threads);
            datagramSocket = new DatagramSocket(port);

            receivers.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        final DatagramPacket receivedDatagramPacket = makeResponsePacket(datagramSocket);
                        datagramSocket.receive(receivedDatagramPacket);
                        senders.submit(() -> {
                            try {
                                datagramSocket.send(
                                        makeRequestPacket(
                                                makeResponse(receivedDatagramPacket),
                                                receivedDatagramPacket.getSocketAddress()
                                        )
                                );
                            } catch (final IOException exception) {
                                System.err.println("Error while sending packet " + exception.getMessage());
                            }
                        });
                    } catch (final SocketException ignored) {
                    } catch (final IOException exception) {
                        System.err.println("Error while receiving packet" + exception.getMessage());
                    }
                }
            });
        } catch (final SocketException exception) {
            System.err.println("Error while creating socket " + exception.getMessage());
        }
    }

    @Override
    public void close() {
        Packer.close(receivers, 10L);
        Packer.close(senders, 10L);
        datagramSocket.close();
    }

}