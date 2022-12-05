package aoc;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day05 {
    ArrayList<Stack<Character>> p1_stacks = new ArrayList<>();
    ArrayList<Stack<Character>> p2_stacks = new ArrayList<>();

    private void parse_stacks(String[] input) {
        // Number of chars is 4 * N - 1 = length where N is the number of stacks
        // N = (L + 1) / 4
        int numStacks = (input[0].length() + 1) / 4;
        for (int i = 0; i < numStacks; i += 1) {
            int position = i * 4 + 1;
            p1_stacks.add(new Stack<>());
            p2_stacks.add(new Stack<>());
            for (int j = input.length - 2; j >= 0; j--) {
                char ch = input[j].charAt(position);
                if (ch == ' ') {
                    break;
                } else if (!Character.isUpperCase(ch)) {
                    throw new RuntimeException("Expected uppercase character");
                }
                p1_stacks.get(i).add(ch);
                p2_stacks.get(i).add(ch);
            }
        }
    }

    private void parse(String filename) {
        try {
            String content = Files.readString(Paths.get(filename));
            String[] halves = content.split("\n\n");
            String[] stacks = halves[0].split("\n");
            parse_stacks(stacks);
            String[] moves = halves[1].split("\n");
            do_moves(moves);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // move 1 from 5 to 2
    // move %d from %d to %d
    private void do_moves(String[] moves) {
        String regex = "^move (\\d+) from (\\d+) to (\\d+)$";
        Pattern pattern = Pattern.compile(regex);
        for (String move : moves) {
            if (move.isEmpty()) {
                continue;
            }
            Matcher matcher = pattern.matcher(move);
            if (!matcher.find()) {
                throw new RuntimeException("matcher didn't find match");
            }
            int n = Integer.parseInt(matcher.group(1));
            int from = Integer.parseInt(matcher.group(2)) - 1;
            int to = Integer.parseInt(matcher.group(3)) - 1;

            char[] chars = new char[n];
            for (int j = 0; j < n; j++) {
                // p1
                char ch = p1_stacks.get(from).pop();
                p1_stacks.get(to).add(ch);
                chars[j] = p2_stacks.get(from).pop();
            }
            for (int j = chars.length - 1; j >= 0; j--) {
                p2_stacks.get(to).add(chars[j]);
            }
        }
    }

    private String[] topString() {
        StringBuilder p1 = new StringBuilder();
        StringBuilder p2 = new StringBuilder();
        for (int i = 0; i < p1_stacks.size(); i++) {
            p1.append(p1_stacks.get(i).peek());
            p2.append(p2_stacks.get(i).peek());
        }
        String[] parts = new String[2];
        parts[0] = p1.toString();
        parts[1] = p2.toString();
        return parts;
    }

    public static void main(String[] args) {
        Day05 day05 = new Day05();
        day05.parse("inputs/05.txt");
        String[] parts = day05.topString();
        System.out.printf("p1: %s\n", parts[0]);
        System.out.printf("p2: %s\n", parts[1]);
    }
}
