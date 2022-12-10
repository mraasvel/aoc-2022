package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

interface Operation {
    int execute(int x);
    void doWork();
    int cycleTimeLeft();
    void reset();

    static Operation fromLine(String line) {
        if (line.equals("noop")) {
            return new NoOp();
        } else {
            String[] parts = line.split(" ");
            if (!parts[0].equals("addx")) {
                throw new RuntimeException("Expected 'addx', got: " + parts[0]);
            }
            return new Add(Integer.parseInt(parts[1]));
        }
    }
}

class NoOp implements Operation {
    private int timeLeft = 1;
    public int cycleTimeLeft() {
        return timeLeft;
    }

    public int execute(int x) {
        return x;
    }

    public void doWork() {
        if (timeLeft == 0) throw new IllegalStateException();
        timeLeft -= 1;
    }

    public void reset() {
        timeLeft = 1;
    }
}

class Add implements Operation {
    private final int value;
    private int timeLeft;

    Add(int value) {
        this.value = value;
        this.timeLeft = 2;
    }
    public int cycleTimeLeft() {
        return timeLeft;
    }

    public void doWork() {
        if (timeLeft == 0) throw new IllegalStateException();
        timeLeft -= 1;
    }

    public int execute(int x) {
        return x + value;
    }

    public void reset() {
        timeLeft = 2;
    }
}

public class Day10 {
    ArrayList<Operation> ops;
    int x = 1; // register
    int nextOp = 0;

    Day10(String filename) {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            this.ops = lines.map(Operation::fromLine).collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    Operation nextOperation() {
        if (nextOp == ops.size()) {
            return null;
        }
        Operation op = ops.get(nextOp);
        nextOp += 1;
        return op;
    }

    void reset() {
        x = 1;
        nextOp = 0;
        ops.forEach(Operation::reset);
    }

    int partOne() {
        int sum = 0;
        HashSet<Integer> cycles = new HashSet<>() {{
            add(20);
            add(60);
            add(100);
            add(140);
            add(180);
            add(220);
        }};
        reset();
        Operation current = nextOperation();
        for (int i = 1; i <= 240; i++) {
            if (cycles.contains(i)) {
                int signalStrength = x * i;
                sum += signalStrength;
            }
            current.doWork();
            if (current.cycleTimeLeft() == 0) {
                x = current.execute(x);
                current = nextOperation();
                if (current == null) break;
            }
        }
        return sum;
    }

    void drawPixel(int position) {
        if (Math.abs(position - x) <= 1) {
            System.out.print("#");
        } else {
            System.out.print(".");
        }
    }

    void partTwo() {
        reset();
        Operation current = nextOperation();
        for (int i = 0; i < 240; i++) {
            if (i != 0 && i % 40 == 0) {
                System.out.println();
            }
            drawPixel(i % 40);
            current.doWork();
            if (current.cycleTimeLeft() == 0) {
                x = current.execute(x);
                current = nextOperation();
                if (current == null) break;
            }
        }
    }
    public static void main(String[] args) {
        String filename = "inputs/10.txt";
        Day10 day10 = new Day10(filename);
        int p1 = day10.partOne();
        System.out.printf("p1: %d\n", p1);
        day10.partTwo();
    }
}
