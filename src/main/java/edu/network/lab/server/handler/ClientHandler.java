package edu.network.lab.server.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class ClientHandler implements Runnable {

    private final int NUM_OF_THREADS;
    protected final ExecutorService pool;

    protected ClientHandler(int numOfThreads) {
        this.NUM_OF_THREADS = numOfThreads;
        pool = Executors.newFixedThreadPool(numOfThreads);
    }

    protected int[][] matrixDifference(int[][] matrix1, int[][] matrix2) {
        int[][] result = new int[matrix1.length][matrix1.length];

        int length = matrix1.length;
        int threadStep = length / NUM_OF_THREADS;

        for (int i = 0; i < NUM_OF_THREADS; i++) {
            int start = i * threadStep;
            int end = (i == NUM_OF_THREADS - 1) ? length : start + threadStep;
            pool.submit(new MatrixDifferenceByRows(result, matrix1, matrix2, start, end));
        }

        try {
            pool.shutdown();
            if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS)) {
                System.out.println("Timeout elapsed before termination");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static final class MatrixDifferenceByRows implements Runnable {
        private final int[][] result;
        private final int[][] m1;
        private final int[][] m2;
        private final int start;
        private final int end;

        public MatrixDifferenceByRows(int[][] result, int[][] m1, int[][] m2, int start, int end) {
            this.result = result;
            this.m1 = m1;
            this.m2 = m2;
            this.start = start;
            this.end = Math.min(end, m1.length);
        }

        @Override
        public void run() {
            for (int i = start; i < end; i++) {
                for (int j = 0; j < m1.length; j++) {
                    result[i][j] = m1[i][j] - m2[i][j];
                }
            }
        }
    }
}
