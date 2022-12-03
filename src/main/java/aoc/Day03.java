package aoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Day03 {

    int sum = 0;
    ArrayList<String> lines = new ArrayList<>();

    void partOne(String filename) {
        try {
            Scanner scanner = new Scanner(new File((filename)));
            while (scanner.hasNextLine()) {
                String line = scanner.next();
                handle_rucksack(line);
                lines.add(line);
            }
            System.out.printf("P1: %d\n", sum);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    int getPriority(char ch) {
        if (Character.isUpperCase(ch)) {
            return ch - 'A' + 27;
        } else {
            if (!Character.isLowerCase(ch)) {
                throw new IllegalArgumentException();
            }
            return ch - 'a' + 1;
        }
    }

    void handle_rucksack(String rs) {
        String A = rs.substring(0, rs.length() / 2);
        String B = rs.substring(rs.length() / 2);
        for (int i = 0; i < A.length(); i++) {
            char ch = A.charAt(i);
            if (charAppearsIn(B, ch)) {
                int score = getPriority(ch);
                sum += score;
                break;
            }
        }
    }

    boolean charAppearsIn(String s, char ch) {
        return s.indexOf(ch) != -1;
    }

    void partTwo() {
        if (lines.isEmpty()) {
            throw new IllegalStateException();
        }
        int priority = 0;
        for (int i = 0; i < lines.size(); i += 3) {
            String a = lines.get(i);
            String b = lines.get(i + 1);
            String c = lines.get(i + 2);
            for (int j = 0; j < a.length(); j++) {
                char ch = a.charAt(j);
                if (charAppearsIn(b, ch) && charAppearsIn(c, ch)) {
                    priority += getPriority(ch);
                    break;
                }
            }
        }
        System.out.printf("Priority: %d\n", priority);
    }

    public static void main(String[] args) {
        Day03 day03 = new Day03();
        day03.partOne("inputs/03.txt");
        day03.partTwo();
    }
}
