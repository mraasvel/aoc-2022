package aoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Day01 {
    public void solve(String filename) {
        ArrayList<Integer> elves = new ArrayList<>();
        try {
            int elf = 0;
            Scanner scanner = new Scanner(new File((filename)));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.length() == 0) {
                    elves.add(elf);
                    elf = 0;
                } else {
                    elf += Integer.parseInt(line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Collections.sort(elves);
        Collections.reverse(elves);
        System.out.printf("%d %d %d\n", elves.get(0), elves.get(1), elves.get(2));
        System.out.printf("P2: %d\n", elves.get(0) + elves.get(1) + elves.get(2));
    }

    public static void main(String[] args) {
        new Day01().solve("inputs/01.txt");
    }
}
