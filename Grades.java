import java.util.Scanner;

public class Grades {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int total = 0;

        for (int i = 1; i <= 5; i++) {
            System.out.print("Enter mark " + i + " (0-100): ");
            int mark = sc.nextInt();
            total += mark;
        }

        double avg = total / 5.0;
        String grade;

        if (avg >= 90)      grade = "A+";
        else if (avg >= 80) grade = "A";
        else if (avg >= 70) grade = "B";
        else if (avg >= 60) grade = "C";
        else if (avg >= 50) grade = "D";
        else                grade = "F";

        System.out.println("Total   : " + total);
        System.out.printf("Average : %.2f%n", avg);
        System.out.println("Grade   : " + grade);
        sc.close();
    }
}
