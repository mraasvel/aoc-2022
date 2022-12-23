package aoc.day23;

import aoc.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Day23 {
    static private final Point N = new Point(0, -1);
    static private final Point NE = new Point(1, -1);
    static private final Point NW = new Point(-1, -1);
    static private final Point S = new Point(0, 1);
    static private final Point SE = new Point(1, 1);
    static private final Point SW = new Point(-1, 1);
    static private final Point E = new Point(1, 0);
    static private final Point W = new Point(-1, 0);
    static ArrayDeque<List<Point>> directions = new ArrayDeque<>() {{
        add(new ArrayList<>() {{ add(N); add(NE); add(NW); }});
        add(new ArrayList<>() {{ add(S); add(SE); add(SW); }});
        add(new ArrayList<>() {{ add(W); add(NW); add(SW); }});
        add(new ArrayList<>() {{ add(E); add(NE); add(SE); }});
    }};
    static boolean moved = false;

    static Set<Point> parse(String filename) throws IOException {
        HashSet<Point> set = new HashSet<>();
        List<String> lines = Files.readAllLines(Paths.get(filename));
        Point current = new Point(0, 0);
        for (String line: lines) {
            current = new Point(0, current.y);
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if (ch == '#') {
                    set.add(new Point(current));
                }
                current = current.add(new Point(1, 0));
            }
            current = current.add(new Point(0, 1));
        }
        return set;
    }

    static boolean containsNone(Set<Point> state, Point from, List<Point> deltas) {
        for (Point delta : deltas) {
            if (state.contains(from.add(delta))) {
                return false;
            }
        }
        return true;
    }

    static boolean hasAdjacent(Set<Point> state, Point point) {
        return directions
                .stream()
                .flatMap(List::stream)
                .map(dir -> state.contains(point.add(dir)))
                .reduce(false, (b, r) -> r || b);
    }

    static Set<Point> doRound(Set<Point> state) {
        moved = false;
        // newPosition -> oldPosition
        Map<Point, Point> movedPoints = new HashMap<>();
        Set<Point> takenPoints = new HashSet<>();
        for (Point point: state) {
            Point newPosition = null;
            // check if point can move
            if (hasAdjacent(state, point)) {
                for (List<Point> dir : directions) {
                    if (containsNone(state, point, dir)) {
                        newPosition = point.add(dir.get(0));
                        break;
                    }
                }
            }
            if (newPosition == null) {
                movedPoints.put(point, point);
                continue;
            }
            moved = true;
            if (takenPoints.contains(newPosition)) {
                if (movedPoints.containsKey(newPosition)) {
                    Point oldPosition = movedPoints.remove(newPosition);
                    movedPoints.put(oldPosition, oldPosition);
                }
                movedPoints.put(point, point);
            } else {
                movedPoints.put(newPosition, new Point(point));
                takenPoints.add(newPosition);
            }
        }
        return movedPoints.keySet();
    }

    static Point getMax(Set<Point> elves) {
        return elves.stream().reduce(new Point(Integer.MIN_VALUE, Integer.MIN_VALUE), (acc, p) -> {
            acc.x = Math.max(acc.x, p.x);
            acc.y = Math.max(acc.y, p.y);
            return acc;
        });
    }

    static Point getMin(Set<Point> elves) {
        return elves.stream().reduce(new Point(Integer.MAX_VALUE, Integer.MAX_VALUE), (acc, p) -> {
            acc.x = Math.min(acc.x, p.x);
            acc.y = Math.min(acc.y, p.y);
            return acc;
        });
    }

    static void printElves(Set<Point> elves) {
        Point min = getMin(elves);
        Point max = getMax(elves);
        int n = 0;
        System.out.printf("%s, %s\n", min, max);
        for (int y = min.y; y < max.y + 1; y++) {
            for (int x = min.x; x < max.x + 1; x++) {
                if (elves.contains(new Point(x, y))) {
                    System.out.print('#');
                } else {
                    System.out.print('.');
                    n++;
                }
            }
            System.out.println();
        }
        System.out.printf("empty(%d)\n", n);
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/23.txt";
        Set<Point> elves = parse(filename);
//        printElves(elves);
        int round = 0;
        do {
            elves = doRound(elves);
            directions.add(directions.remove());
            round++;
        } while (moved);
        Point min = getMin(elves);
        Point max = getMax(elves);
        int width = Math.abs(max.x - min.x) + 1;
        int height = Math.abs(max.y - min.y) + 1;
        int squares = width * height - elves.size();
        System.out.printf("%d * %d - %d => %d\n", width, height, elves.size(), squares);
        System.out.println(squares);
        System.out.println(round);
    }
}
