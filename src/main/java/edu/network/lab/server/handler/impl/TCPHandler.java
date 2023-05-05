package edu.network.lab.server.handler.impl;

import edu.network.lab.server.handler.ClientHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;

public class TCPHandler extends ClientHandler {

    private final Socket socket;

    public TCPHandler(Socket socket, int numOfWorkingThreads) {
        super(numOfWorkingThreads);
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try (
                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            out.writeUTF(("Client " + socket.getInetAddress() + ":" + socket.getPort() + " connected" + "\n"));

            int[][] matrix1 = readMatrix(in);
            int[][] matrix2 = readMatrix(in);

            out.writeUTF("Data received correctly");

            while (!in.readUTF().equalsIgnoreCase("start")) {
                System.out.println("Write start");
            }

            CompletableFuture<int[][]> future = CompletableFuture.supplyAsync(() -> matrixDifference(matrix1, matrix2));

            out.writeUTF("Calculations started, pls wait");

            while (!Thread.currentThread().isInterrupted()) {
                if (!in.readUTF().equalsIgnoreCase("get")) {
                    System.out.println("Write get");
                    continue;
                }

                if (!future.isDone()) {
                    out.writeUTF("Running");
                } else {
                    out.writeUTF("Done");
                    break;
                }
            }

            writeMatrix(out, future.get());
        } catch (SocketTimeoutException e) {
            System.out.println("Timeout with: " + this.socket.getInetAddress() + " elapsed!");
        } catch (SocketException e) {
            System.out.println("Connection with: " + this.socket.getInetAddress() + " abandoned!");
        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeMatrix(DataOutputStream out, int[][] matrix) throws IOException {
        out.writeInt(matrix.length);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                out.writeInt(matrix[i][j]);
            }
        }

        out.flush();
    }

    private int[][] readMatrix(DataInputStream in) throws IOException {
        int matrixSize = in.readInt();

        int[][] matrix = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                matrix[i][j] = in.readInt();
            }
        }

        return matrix;
    }
}