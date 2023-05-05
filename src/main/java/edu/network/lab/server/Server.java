package edu.network.lab.server;

import edu.network.lab.server.handler.ClientHandler;
import edu.network.lab.server.handler.impl.TCPHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {

    private final ExecutorService pool;
    private final int port;
    private final int NUM_OF_DIFFERENCE_THREADS;
    private ServerSocket serverSocket;
    private Thread serverThread;

    public Server(int port, int numOfClientThreads, int numOfDifferenceThreads) {
        this.port = port;
        this.pool = Executors.newFixedThreadPool(numOfClientThreads);
        this.NUM_OF_DIFFERENCE_THREADS = numOfDifferenceThreads;
    }

    public void startServer() {
        serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);

                while (!Thread.currentThread().isInterrupted()) {
                    ClientHandler handler = new TCPHandler(serverSocket.accept(), NUM_OF_DIFFERENCE_THREADS);
                    pool.submit(handler);
                }

            } catch (SocketException e) {
                System.out.println("Socket stopped working");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (!Thread.currentThread().isInterrupted())
            serverThread.start();
    }

    public void stopServer() {
        serverThread.interrupt();

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            serverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            pool.shutdown();

            if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                System.out.println("Timeout elapsed before termination");

                pool.shutdownNow();

                if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                    System.err.println("Pool did not terminate");
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();

            pool.shutdownNow();

            Thread.currentThread().interrupt();
        }
    }
}
