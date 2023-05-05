package edu.network.lab;

import edu.network.lab.client.Client;
import edu.network.lab.server.Server;

public class Main {

    private static final int PORT = 1234;
    private static final String HOST = "localhost";
    private static final int NUM_OF_CLIENT_THREADS = 3;
    private static final int NUM_OF_DIFFERENCE_THREADS = 3;


    public static void main(String[] args) throws InterruptedException {
        Server server = new Server(PORT, NUM_OF_CLIENT_THREADS, NUM_OF_DIFFERENCE_THREADS);
        Client client = new Client(HOST, PORT);

        serverTest(server, client);
    }

    public static void serverTest(Server server, Client client) throws InterruptedException {
        server.startServer();

        Thread.sleep(1000);

        new Thread(client::sendMessage).start();

        Thread.sleep(200_000);

        server.stopServer();
    }
}