package aoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Scanner;

public class Day01 {
    ArrayList<Integer> elves = new ArrayList<>();

    // Minheap for optimal space usage
    // Stores at most MAX_HEAP_SIZE and contains the largest MAX_HEAP_SIZE elves
    PriorityQueue<Integer> top_elves = new PriorityQueue<>();
    static final int MAX_HEAP_SIZE = 3;

    void addToTop(int elf) {
        top_elves.add(elf);
        if (top_elves.size() > MAX_HEAP_SIZE) {
            top_elves.remove();
        }
    }

    void parse(String filename) {
        try {
            int elf = 0;
            Scanner scanner = new Scanner(new File((filename)));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.length() == 0) {
                    elves.add(elf);
                    addToTop(elf);
                    elf = 0;
                } else {
                    elf += Integer.parseInt(line);
                }
            }
            if (elf != 0) {
                elves.add(elf);
                addToTop(elf);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    void solve() {
        if (elves.isEmpty()) {
            throw new RuntimeException();
        }

        // first solution attempt
//        Collections.sort(elves);
//        Collections.reverse(elves);
//        int max = elves[0];
//        int top3 = elves[0] + elves[1] + elves[2];

        // functional stream stolen from Tijmen
        int p1 = elves.stream().mapToInt(a -> a).max().getAsInt();
        int p2 = elves.stream().sorted(Collections.reverseOrder()).limit(3).reduce(0, Integer::sum);

        // PQ solution
        // PQ contains at most 3 elements making it O(1) in terms of space complexity
        // The largest element is the third element
        assert top_elves.size() == 3;
        assert p1 == top_elves.stream().skip(2).iterator().next();
        assert p2 == top_elves.stream().reduce(0, Integer::sum);

        System.out.printf("P1: %d\n", p1);
        System.out.printf("P2: %d\n", p2);
    }

    public static void main(String[] args) {
        Day01 day01 = new Day01();
        day01.parse("inputs/01.txt");
        day01.solve();
    }
}
