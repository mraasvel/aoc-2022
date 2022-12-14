package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

class Move {
    private final int amount;
    private final Point direction;

    Move(String line) {
        String[] parts = line.split(" ");
        this.amount = Integer.parseInt(parts[1]);
        this.direction = switch (parts[0]) {
            case "R":
                yield new Point(1, 0);
            case "L":
                yield new Point(-1, 0);
            case "U":
                yield new Point(0, 1);
            case "D":
                yield new Point(0, -1);
            default:
                throw new IllegalArgumentException("invalid line: " + line);
        };
    }

    public int getAmount() {
        return amount;
    }

    public Point getDelta() {
        return direction;
    }
}

public class Day09 {
    final int NUM_TAILS = 9;
    ArrayList<Move> moves;
    HashSet<Point> visited = new HashSet<>();
    Day09(String filename) {
        try {
            this.moves = Files.readAllLines(Paths.get(filename)).stream().map(Move::new).collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    static Point updateTail(Point head, Point tail) {
        if (head.isAdjacent(tail)) {
            return tail;
        } else {
            Point delta = tail.directionTo(head);
            return tail.add(delta);
        }
    }

    public static void main(String[] args) {
        String filename = "inputs/09.txt";
        Day09 day09 = new Day09(filename);
        int p1 = day09.partOne();
        int p2 = day09.partTwo();
        System.out.printf("p1: %d\n", p1);
        System.out.printf("p2: %d\n", p2);
    }

    int partOne() {
        Point head = new Point(0, 0);
        Point tail = new Point(0, 0);
        visited.add(new Point(tail));
        for (Move move : moves) {
            Point delta = move.getDelta();
            for (int i = 0; i < move.getAmount(); i++) {
                head = head.add(delta);
                tail = updateTail(head, tail);
                visited.add(new Point(tail));
            }
        }
        return visited.size();
    }

    int partTwo() {
        Point head = new Point(0, 0);
        ArrayList<Point> parts = new ArrayList<>();
        for (int i = 0; i < NUM_TAILS; i++) {
            parts.add(new Point(0, 0));
        }
        visited.clear();
        visited.add(new Point(0, 0));
        for (Move move : moves) {
            Point delta = move.getDelta();
            for (int i = 0; i < move.getAmount(); i++) {
                head = head.add(delta);
                Point newHead = new Point(head);
                for (int j = 0; j < NUM_TAILS; j++) {
                    Point tail = new Point(parts.get(j));
                    Point newTail = updateTail(newHead, tail);
                    parts.set(j, newTail);
                    newHead = new Point(newTail);
                }
                visited.add(new Point(parts.get(NUM_TAILS - 1)));
            }
        }
        return visited.size();
    }
}
