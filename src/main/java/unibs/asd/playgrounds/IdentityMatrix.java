package unibs.asd.playgrounds;

public class IdentityMatrix {

    public static boolean[][] create(int size) {
        boolean[][] matrix = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            matrix[i][i] = true;
        }
        return matrix;
    }
}
