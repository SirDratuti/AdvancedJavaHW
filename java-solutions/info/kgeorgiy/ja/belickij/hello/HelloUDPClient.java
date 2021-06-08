package info.kgeorgiy.ja.belickij.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static info.kgeorgiy.ja.belickij.hello.Packer.*;

public class HelloUDPClient implements HelloClient {

    private final AtomicInteger atomicInteger;

    public static void main(final String[] args) {
        if (checkInput(args, 5)) {
            startClient(new HelloUDPClient(), args);
        } else {
            throw new IllegalArgumentException("Illegal input arguments");
        }
    }


    public HelloUDPClient() {
        atomicInteger = new AtomicInteger(0);
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        final ExecutorService executorService = Executors.newFixedThreadPool(threads);
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);

        final Runnable runnable = () -> {

            try (final DatagramSocket datagramSocket = new DatagramSocket()) {
                datagramSocket.setSoTimeout(200);
                final DatagramPacket responseDatagramPacket = makeResponsePacket(datagramSocket);
                final int threadNumber = atomicInteger.getAndIncrement();

                for (int i = 0; i < requests; i++) {
                    final String requestString = makeRequestString(prefix, threadNumber, i);
                    final DatagramPacket requestDatagramPacket = makeRequestPacket(requestString, inetSocketAddress);
                    boolean isCorrect = false;
                    while (!isCorrect) {
                        try {
                            datagramSocket.send(requestDatagramPacket);
                            datagramSocket.receive(responseDatagramPacket);

                            final String answer = getAnswerString(responseDatagramPacket);
                            if (checkString(answer, requestString)) {
                                isCorrect = true;
                            }

                        } catch (final IOException ignored) {
                        }
                    }
                }
            } catch (final SocketException exception) {
                System.err.println(exception.getMessage());
            }
        };

        for (int i = 0; i < threads; i++) {
            executorService.submit(new Thread(runnable));
        }

        close(executorService, Long.MAX_VALUE);
    }


}
