import java.util.Scanner;

public class MatrixAdd {
    static int[][] readMatrix(Scanner sc, int r, int c, String name) {
        int[][] m = new int[r][c];
        System.out.println("Enter elements of matrix " + name + ":");
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                m[i][j] = sc.nextInt();
        return m;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Rows and columns of A: ");
        int r1 = sc.nextInt(), c1 = sc.nextInt();
        System.out.print("Rows and columns of B: ");
        int r2 = sc.nextInt(), c2 = sc.nextInt();

        // dimension check first
        if (r1 != r2 || c1 != c2) {
            System.out.println("Addition not possible: dimensions differ.");
            return;
        }

        int[][] A = readMatrix(sc, r1, c1, "A");
        int[][] B = readMatrix(sc, r2, c2, "B");
        int[][] C = new int[r1][c1];

        for (int i = 0; i < r1; i++)
            for (int j = 0; j < c1; j++)
                C[i][j] = A[i][j] + B[i][j];

        System.out.println("A + B =");
        for (int i = 0; i < r1; i++) {
            for (int j = 0; j < c1; j++)
                System.out.print(C[i][j] + "\t");
            System.out.println();
        }
        sc.close();
    }
}
