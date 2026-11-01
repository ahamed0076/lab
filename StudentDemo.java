import java.util.Scanner;

class Student {
    int regNo;
    String name;
    int[] marks;

    Student(int regNo, String name, int[] marks) {
        this.regNo = regNo;
        this.name = name;
        this.marks = marks;
    }

    int calcTotal() {
        int total = 0;
        for (int m : marks) total += m;
        return total;
    }

    double calcAvg() {
        return calcTotal() / (double) marks.length;
    }

    void display() {
        System.out.println("Reg No  : " + regNo);
        System.out.println("Name    : " + name);
        System.out.println("Total   : " + calcTotal());
        System.out.printf("Average : %.2f%n", calcAvg());
    }
}

public class StudentDemo {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Number of students: ");
        int count = sc.nextInt();
        System.out.print("Number of subjects: ");
        int subjects = sc.nextInt();

        Student[] students = new Student[count];

        for (int i = 0; i < count; i++) {
            System.out.println("\n--- Student " + (i + 1) + " ---");
            System.out.print("Reg No: ");
            int reg = sc.nextInt();
            sc.nextLine();                       // clear newline
            System.out.print("Name: ");
            String name = sc.nextLine();

            int[] marks = new int[subjects];
            for (int j = 0; j < subjects; j++) {
                System.out.print("Mark " + (j + 1) + ": ");
                marks[j] = sc.nextInt();
            }
            students[i] = new Student(reg, name, marks);
        }

        System.out.println("\n===== Results =====");
        for (Student s : students) {
            s.display();
            System.out.println();
        }
        sc.close();
    }
}
