package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

class Rock {
    List<Point> turns = new ArrayList<>();
    int largestX = 0;
    int largestY = 0;

    static Rock fromLine(String line) {
        Rock rock = new Rock();
        Matcher matcher = Pattern.compile("(\\d+),(\\d+)").matcher(line);
        while (matcher.find()) {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            rock.addPoint(new Point(x, y));
        }
        return rock;
    }

    void addPoint(Point point) {
        largestX = Math.max(point.x, largestX);
        largestY = Math.max(point.y, largestY);
        turns.add(point);
    }

    int getLargestX() {
        return largestX;
    }

    int getLargestY() {
        return largestY;
    }

    List<Point> getAllPoints() {
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 1; i < turns.size(); i++) {
            Point current = new Point(turns.get(i - 1));
            Point end = new Point(turns.get(i));
            Point delta = current.directionTo(end);
            while (!current.equals(end)) {
                points.add(current);
                current = current.add(delta);
            }
            points.add(end);
        }
        return points;
    }
}

public class Day14 {
    enum FillType {
        Sand,
        Rock,
        Empty
    }
    int width;
    int height;
    HashMap<Point, FillType> grid = new HashMap<>();
    Day14(String filename) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            List<Rock> rocks = lines.map(Rock::fromLine).collect(Collectors.toCollection(ArrayList::new));
            width = rocks.stream().map(Rock::getLargestX).max(Integer::compare).get() + 1;
            height = rocks.stream().map(Rock::getLargestY).max(Integer::compare).get() + 1;
            rocks.stream().flatMap(rock -> rock.getAllPoints().stream()).forEach(point -> setType(point, FillType.Rock));
        }
    }

    private boolean partTwo = false;

    void setPartTwo() {
        resetSand();
        height += 2;
        partTwo = true;
    }

    void resetSand() {
        grid.entrySet().forEach(entry -> {
            if (entry.getValue() == FillType.Sand) {
                entry.setValue(FillType.Empty);
            }
        });
    }

    boolean isInBounds(Point point) {
        if (partTwo) {
            return point.y < height;
        } else {
            return point.x >= 0 && point.y >= 0 && point.x < width && point.y < height;
        }
    }

    FillType getType(Point point) {
        if (partTwo && point.y == height - 1) {
            return FillType.Rock;
        }
        return grid.getOrDefault(point, FillType.Empty);
    }

    void setType(Point point, FillType type) {
        grid.put(point, type);
    }

    boolean isBlocked(Point point) {
        return getType(point) != FillType.Empty;
    }

    boolean introduceSand(Point position) {
        if (isBlocked(position)) {
            return false;
        }
        Point deltaDown = new Point(0, 1);
        Point deltaLeft = new Point(-1, 1);
        Point deltaRight = new Point(1, 1);
        while (isInBounds(position)) {
            if (!isBlocked(position.add(deltaDown))) {
                position = position.add(deltaDown);
            } else if (!isBlocked(position.add(deltaLeft))) {
                position = position.add(deltaLeft);
            } else if (!isBlocked(position.add(deltaRight))) {
                position = position.add(deltaRight);
            } else {
                setType(position, FillType.Sand);
                return true;
            }
        }
        return false;
    }

    int partOne() {
        int numSand = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if (!introduceSand(new Point(500, 0))) {
                break;
            }
            numSand++;
        }
        return numSand;
    }

    int partTwo() {
        return partOne();
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/14.txt";
        Day14 solver = new Day14(filename);
        int p1 = solver.partOne();
        System.out.printf("p1: %d\n", p1);
        solver.setPartTwo();
        int p2 = solver.partTwo();
        System.out.printf("p2: %d\n", p2);
    }
}
