package aoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class InclusiveRange {
    private final int low;
    private final int high;

    // <range> = INTEGER "-" INTEGER
    public InclusiveRange(String range) {
        String[] numbers = range.split("-");
        this.low = Integer.parseInt(numbers[0]);
        this.high = Integer.parseInt(numbers[1]);
    }

    public InclusiveRange(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public boolean contains(InclusiveRange other) {
        return this.low <= other.low && this.high >= other.high;
    }

    public boolean contains(int n) {
        return this.low <= n && this.high >= n;
    }

    public boolean overlaps(InclusiveRange other) {
        return this.contains(other.low) || this.contains(other.high);
    }
}

public class Day04 {

    public static boolean fully_contains(InclusiveRange a, InclusiveRange b) {
        return a.contains(b) || b.contains(a);
    }

    public static boolean overlaps(InclusiveRange a, InclusiveRange b) {
        return a.overlaps(b) || b.overlaps(a);
    }

    public void solve(String filename) {
        int partOne = 0;
        int partTwo = 0;
        try {
            Scanner scanner = new Scanner(new File(filename));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                // <line> = <range> "," <range>
                String[] ranges = line.split(",");
                if (ranges.length != 2) {
                    throw new RuntimeException("expected two ranges per line");
                }
                InclusiveRange a = new InclusiveRange(ranges[0]);
                InclusiveRange b = new InclusiveRange(ranges[1]);
                if (fully_contains(a, b)) {
                    partOne += 1;
                }
                if (overlaps(a, b)) {
                    partTwo += 1;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.printf("P1: %d\n", partOne);
        System.out.printf("P2: %d\n", partTwo);
    }

    public static void main(String[] args) {
        Day04 day04 = new Day04();
        day04.solve("inputs/04.txt");
    }
}
