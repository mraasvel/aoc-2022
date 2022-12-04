package aoc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Day03 {
    void partOne(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            int sum = reader.lines().map(Day03::handle_rucksack).reduce(0, Integer::sum);
            System.out.printf("P1: %d\n", sum);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static int getPriority(char ch) {
        if (Character.isUpperCase(ch)) {
            return ch - 'A' + 27;
        } else {
            if (!Character.isLowerCase(ch)) {
                throw new IllegalArgumentException();
            }
            return ch - 'a' + 1;
        }
    }

    static int handle_rucksack(String rs) {
        String A = rs.substring(0, rs.length() / 2);
        Set<Character> B = rs.substring(rs.length() / 2).chars().mapToObj(ch -> (char)ch).collect(Collectors.toSet());
        int score = 0;
        for (int i = 0; i < A.length(); i++) {
            char ch = A.charAt(i);
            if (B.contains(ch)) {
                score = getPriority(ch);
                break;
            }
        }
        return score;
    }

    void partTwo(String filename) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            int priority = 0;
            for (int i = 0; i < lines.size(); i += 3) {
                String a = lines.get(i);
                Set<Character> b = lines.get(i + 1).chars().mapToObj(ch -> (char)ch).collect(Collectors.toSet());
                Set<Character> c = lines.get(i + 2).chars().mapToObj(ch -> (char)ch).collect(Collectors.toSet());
                for (int j = 0; j < a.length(); j++) {
                    char ch = a.charAt(j);
                    if (b.contains(ch) && c.contains(ch)) {
                        priority += getPriority(ch);
                        break;
                    }
                }
            }
            System.out.printf("Priority: %d\n", priority);
        } catch (IOException e) {
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Day03 day03 = new Day03();
        String filename = "inputs/03.txt";
        day03.partOne(filename);
        day03.partTwo(filename);
    }
}
