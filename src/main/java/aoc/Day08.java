package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day08 {
    String[] grid;
    int height;
    int width;

    Day08(String filename) {
        try {
            grid = Files.readString(Paths.get(filename)).split("\n");
            height = grid.length;
            width = grid[0].length();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    int getTreeHeight(int x, int y) {
        if (!isValidIndex(x, y)) {
            return -1;
        }
        return grid[y].charAt(x) - '0';
    }

    boolean isValidIndex(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    int maxTree(int x, int y, int dx, int dy) {
        int largest = -1;
        x += dx;
        y += dy;
        while (isValidIndex(x, y)) {
            largest = Integer.max(largest, getTreeHeight(x, y));
            x += dx;
            y += dy;
        }
        return largest;
    }

    int partOne() {
        int seen = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = getTreeHeight(x, y);
                if (value > maxTree(x, y, -1, 0)
                    || value > maxTree(x, y, 1, 0)
                    || value > maxTree(x, y, 0, 1)
                    || value > maxTree(x, y, 0, -1)
                ) {
                    seen += 1;
                }
            }
        }
        return seen;
    }

    int treesVisibleFrom(int x, int y, int value, int dx, int dy) {
        int seen = 0;
        x += dx;
        y += dy;
        while (isValidIndex(x, y)) {
            int h = getTreeHeight(x, y);
            seen += 1;
            if (h >= value) {
                break;
            }
            x += dx;
            y += dy;
        }
        return seen;
    }

    int getScenicScore(int x, int y) {
        int value = getTreeHeight(x, y);
        return treesVisibleFrom(x, y, value, 1, 0)
            * treesVisibleFrom(x, y, value, -1, 0)
            * treesVisibleFrom(x, y, value, 0, 1)
            * treesVisibleFrom(x, y, value, 0, -1);
    }

    int partTwo() {
        int p2 = -1;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int score = getScenicScore(x, y);
                p2 = Integer.max(p2, score);
            }
        }
        return p2;
    }

    public static void main(String[] args) {
        String filename = "inputs/08.txt";
        Day08 day08 = new Day08(filename);
        int p1 = day08.partOne();
        int p2 = day08.partTwo();
        System.out.printf("p1: %d\n", p1);
        System.out.printf("p2: %d\n", p2);
    }
}
