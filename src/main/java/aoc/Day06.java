package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Day06 {

    static private boolean isMarker(String s, int position, int n) {
        return s.substring(position - n + 1, position + 1)
                .chars()
                .mapToObj(ch -> (char)ch)
                .collect(Collectors.toSet())
                .size() == n;
    }

    private static int doPart(String line, int n) {
        for (int i = n - 1; i < line.length(); i++) {
            if (isMarker(line, i, n)) {
                return i + 1;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        String filename = "inputs/06.txt";
        try {
            String line = Files.readString(Paths.get(filename));
            int p1 = doPart(line, 4);
            int p2 = doPart(line, 14);
            System.out.printf("p1: %d\n", p1);
            System.out.printf("p2: %d\n", p2);
        } catch (IOException e) {
            System.exit(1);
        }
    }
}
