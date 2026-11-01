import java.util.Scanner;

public class ArrayStats {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter N: ");
        int n = sc.nextInt();
        if (n <= 0) {
            System.out.println("N must be positive.");
            return;
        }

        int[] arr = new int[n];
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        int sum = 0;

        System.out.println("Enter " + n + " numbers:");
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
            sum += arr[i];
            if (arr[i] > max) max = arr[i];
            if (arr[i] < min) min = arr[i];
        }

        double avg = sum / (double) n;

        System.out.println("Max     : " + max);
        System.out.println("Min     : " + min);
        System.out.println("Sum     : " + sum);
        System.out.printf("Average : %.2f%n", avg);
        sc.close();
    }
}
