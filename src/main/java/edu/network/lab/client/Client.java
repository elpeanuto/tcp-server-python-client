package edu.network.lab.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public final class Client {

    private final String HOST;
    private final int PORT;
    private final Random RANDOM = new Random();
    private int matrixSize = 3;

    public Client(String host, int port) {
        this.HOST = host;
        this.PORT = port;
    }

    private int[][] generateSquareMatrix(int size) {
        return IntStream.range(0, size)
                .mapToObj(row -> IntStream.range(0, size)
                        .map(col -> RANDOM.nextInt(10))
                        .toArray())
                .toArray(int[][]::new);
    }

    public void sendMessage() {
        try (Socket socket = new Socket(HOST, PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            System.out.println("Server response: " + in.readUTF());

            int[][] matrix1 = generateSquareMatrix(matrixSize);
            int[][] matrix2 = generateSquareMatrix(matrixSize);

            writeMatrix(out, matrix1);
            writeMatrix(out, matrix2);

            System.out.println("Server response: " + in.readUTF());

            out.writeUTF("Start");

            System.out.println("\nServer response: " + in.readUTF() + "\n");

            while (true) {
                out.writeUTF("Get");
                String str = in.readUTF();

                if (str.equalsIgnoreCase("running")) {
                    System.out.println("Server response: " + str);
                } else if (str.equalsIgnoreCase("done")) {
                    System.out.println("\nServer response: " + Arrays.deepToString(readMatrix(in)));
                    break;
                }
            }
        } catch (SocketException e) {
            System.out.println("Connection with server abandoned!");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void writeMatrix(DataOutputStream out, int[][] matrix) throws IOException {
        out.writeInt(matrix.length);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                out.writeInt(matrix[i][j]);
            }
        }
    }

    private int[][] readMatrix(DataInputStream in) throws IOException {
        int size = in.readInt();

        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = in.readInt();
            }
        }
        return matrix;
    }
}

