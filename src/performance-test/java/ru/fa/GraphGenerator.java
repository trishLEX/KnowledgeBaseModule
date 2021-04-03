package ru.fa;

import java.util.Arrays;
import java.util.Random;

public class GraphGenerator {

    private static final int VERTICES = 5;
    private static final int COMPONENTS = 1;

    private static final int CHILD_SIZE = 2;
    private static final Random RANDOM = new Random();

    public static void main (String[] args) {
        for (int i = 0; i < COMPONENTS; i++) {
            int[][] table = new int[VERTICES][VERTICES];
//            for (int row = 0; row < VERTICES; row++) {
//                Arrays.fill(table[row], -1);
//            }


            for (int row = 0; row < VERTICES-1; row++) {
                int childSize = nextInt(1, CHILD_SIZE + 1);
                int[] childsIdx = new int[childSize];
                for (int child = 0; child < childSize; child++) {
                    childsIdx[child] = nextInt(row + 1);
                }
                for (int childIdx = 0; childIdx < childSize; childIdx++) {
                    table[row][childsIdx[childIdx]] = 1;
                }
            }

            System.out.println(tableToString(table));
        }
    }

    private static void createChildren(int i, int j, int[][] table) {
        int childSize = nextInt(1, CHILD_SIZE + 1);
        int[] childsIdx = new int[childSize];
        for (int child = 0; child < childSize; child++) {
            childsIdx[child] = nextInt(i + 1);
        }
    }

    private static String tableToString(int[][] table) {
        String lineSeparator = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        for (int[] row : table) {
            sb.append(Arrays.toString(row)).append(lineSeparator);
        }

        return sb.toString();
    }

    private static int nextInt(int min) {
        return nextInt(min, VERTICES);
    }
    private static int nextInt() {
        return RANDOM.nextInt(2);
    }
    private static int nextInt(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }
}
