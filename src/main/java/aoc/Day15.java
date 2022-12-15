package aoc;

// Sensor at x=2, y=18: closest beacon is at x=-2, y=15

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Range {
    int start;
    int end;

    Range() {
        // empty range
        this.start = 0;
        this.end = 0;
    }

    Range(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException();
        }
        this.start = start;
        this.end = end;
    }

    @Override()
    public String toString() {
        return String.format("[%d, %d)", start, end);
    }

    public boolean overlaps(Range other) {
        // [0, 5), [5, 10) -> FALSE
        // [0, 5), [4, 10) -> TRUE
        // [0, 5), [-5, 0) -> FALSE
        // [0, 5), [-5, 1) -> TRUE
        return !(other.end <= start || other.start >= end);
    }

    // [0, 5) - [1, 3) -> [0, 1), [3, 5)
    // [0, 5) - [<=0, 3) -> [3, 5) (b.end, a.end);
    // [0, 5) - [3, >=5) -> [0, 3) (a.start, b.start);
    // [0, 5) - [<=0, >= 5] -> nothing
    // [0, 21) -
    // precondition: there is overlap
    public List<Range> subtract(Range other) {
        List<Range> ranges = new ArrayList<>();
        if (other.start <= start && other.end >= end) {
            return ranges;
        } else if (other.start <= start) {
            ranges.add(new Range(other.end, end));
        } else if (other.end >= end) {
            ranges.add(new Range(start, other.start));
        } else {
            ranges.add(new Range(start, other.start));
            ranges.add(new Range(other.end, end));
        }
        return ranges;
    }
}

class Sensor {
    private final Point position;
    private final Point closestBeacon;
    private final int distance;

    Sensor(Point position, Point closestBeacon) {
        this.position = position;
        this.closestBeacon = closestBeacon;
        this.distance = position.manhattanDistance(closestBeacon);
    }

    static Sensor fromLine(String line) {
        Matcher matcher = Pattern.compile("x=(-?\\d+), y=(-?\\d+)").matcher(line);
        if (!matcher.find()) {
            throw new RuntimeException("didn't find first position");
        }
        Point position = new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        if (!matcher.find()) {
            throw new RuntimeException("didn't find second position");
        }
        Point beacon = new Point(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        return new Sensor(position, beacon);
    }

    static final int SELECTED_ROW = 2000000;
//    static final int SELECTED_ROW = 10;

    // returns list of all points that cannot contain a beacon
    List<Point> emptyPointsOnRow() {
        ArrayList<Point> points = new ArrayList<>();
        int distance = this.position.manhattanDistance(this.closestBeacon);
        for (int x = position.x - distance; x <= position.x + distance; x++) {
            Point current = new Point(x, SELECTED_ROW);
            if (current.equals(this.position) || current.equals(this.closestBeacon)) {
                continue;
            }
            if (current.manhattanDistance(this.position) <= distance) {
                points.add(current);
            }
        }
        return points;
    }

    Range rangeOnLine(int line) {
        int lineDistance = Math.abs(position.y - line);
        if (lineDistance > distance) {
            // false: doesn't reach line
            return new Range();
        }
        int covered = 1 + (distance - lineDistance) * 2;
        return new Range(position.x - covered / 2, position.x + covered / 2 + 1);
    }
}

public class Day15 {
    List<Sensor> sensors;
    Day15(String filename) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            this.sensors = lines.map(Sensor::fromLine).collect(Collectors.toCollection(ArrayList::new));
        }
    }

    int partOne() {
        Set<Point> set = sensors.stream().flatMap(sensor -> sensor.emptyPointsOnRow().stream()).collect(Collectors.toSet());
        return set.size();
    }

    static final int MAX_VALUES = 4000000;
//    static final int MAX_VALUES = 20;
    static final int FORTY_MILLION = 4000000;

    List<Range> coversRange(List<Range> ranges, Range range) {
        List<Range> uncovered = new ArrayList<>(){{ add(range); }};
        for (Range x: ranges) {
            List<Range> newUncovered = new ArrayList<>();
            for (Range y : uncovered) {
                if (!y.overlaps(x)) {
                    newUncovered.add(y);
                } else {
                    newUncovered.addAll(y.subtract(x));
                }
            }
            uncovered = newUncovered;
        }
        return uncovered;
    }

    long partTwo() {
        Range range = new Range(0, MAX_VALUES + 1);
        for (int y = 0; y <= MAX_VALUES; y++) {
            final int line = y;
            ArrayList<Range> ranges = sensors.stream().map(sensor -> sensor.rangeOnLine(line)).collect(Collectors.toCollection(ArrayList::new));
            List<Range> uncovered = coversRange(ranges, range);
            if (uncovered.size() != 0) {
                Range r = uncovered.get(0);
                long x = r.start;
                System.out.printf("(%d, %d)\n", x, y);
                return x * (long)FORTY_MILLION + (long)y;
            }
        }
        return -1;
    }
    public static void main(String[] args) throws IOException {
        String filename = "inputs/15.txt";
        Day15 solver = new Day15(filename);
        int p1 = solver.partOne();
        System.out.println(p1);
        long p2 = solver.partTwo();
        System.out.println(p2);
    }
}
