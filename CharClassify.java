import java.util.Scanner;

public class CharClassify {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a string: ");
        String s = sc.nextLine();

        int vowels = 0, consonants = 0, digits = 0, spaces = 0, others = 0;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (Character.isLetter(ch)) {
                switch (Character.toLowerCase(ch)) {
                    case 'a': case 'e': case 'i': case 'o': case 'u':
                        vowels++;
                        break;
                    default:
                        consonants++;
                }
            } else if (Character.isDigit(ch)) {
                digits++;
            } else if (Character.isWhitespace(ch)) {
                spaces++;
            } else {
                others++;
            }
        }

        System.out.println("Vowels      : " + vowels);
        System.out.println("Consonants  : " + consonants);
        System.out.println("Digits      : " + digits);
        System.out.println("Whitespaces : " + spaces);
        System.out.println("Others      : " + others);
        sc.close();
    }
}
