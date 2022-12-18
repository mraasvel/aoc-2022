package aoc.day18;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day18 {
    static private final Point3D[] ADJACENT_DELTAS = {
            new Point3D(1, 0, 0),
            new Point3D(-1, 0, 0),
            new Point3D(0, 1, 0),
            new Point3D(0, -1, 0),
            new Point3D(0, 0, 1),
            new Point3D(0, 0, -1),
    };
    private final HashSet<Point3D> points = new HashSet<>();
    Point3D minimumValues = null;
    Point3D maximumValues = null;
    private int partOneSurface = 0;

    Day18(String filename) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            lines.map(Day18::parseLine).forEach(point -> {
                if (points.contains(point)) {
                    throw new RuntimeException("point already in map");
                }
                checkMinMax(point);
                int numNeighbours = (int)adjacentPoints(point).stream().filter(points::contains).count();
                this.partOneSurface += 6 - 2 * numNeighbours;
                points.add(point);
            });
            this.minimumValues = this.minimumValues.add(new Point3D(-1, -1, -1));
            this.maximumValues = this.maximumValues.add(new Point3D(1, 1, 1));
        }
    }

    static Point3D parseLine(String line) {
        Matcher matcher = Pattern.compile("^(-?\\d+),(-?\\d+),(-?\\d+)$").matcher(line);
        if (!matcher.find()) {
            throw new RuntimeException("invalid line: match not found");
        }
        return new Point3D(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                Integer.parseInt(matcher.group(3))
        );
    }

    static List<Point3D> adjacentPoints(Point3D point) {
        return Arrays.stream(ADJACENT_DELTAS).map(point::add).collect(Collectors.toCollection(ArrayList::new));
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/18.txt";
        Day18 solver = new Day18(filename);
        int p1 = solver.partOne();
        System.out.println(p1);

        int p2 = solver.partTwo();
        System.out.println(p2);
    }

    void checkMinMax(Point3D point) {
        if (minimumValues == null) {
            minimumValues = new Point3D(point);
            maximumValues = new Point3D(point);
            return;
        }
        maximumValues.x = Math.max(point.x, maximumValues.x);
        maximumValues.y = Math.max(point.y, maximumValues.y);
        maximumValues.z = Math.max(point.z, maximumValues.z);
        minimumValues.x = Math.min(point.x, minimumValues.x);
        minimumValues.y = Math.min(point.y, minimumValues.y);
        minimumValues.z = Math.min(point.z, minimumValues.z);
    }

    int partOne() {
        return partOneSurface;
    }

    boolean isInBounds(Point3D p) {
        return p.x >= minimumValues.x && p.x <= maximumValues.x
                && p.y >= minimumValues.y && p.y <= maximumValues.y
                && p.z >= minimumValues.z && p.z <= maximumValues.z;
    }

    int partTwo() {
        HashSet<Point3D> visited = new HashSet<>();
        Point3D start = new Point3D(minimumValues);
        Stack<Point3D> todo = new Stack<>() {{ add(start); }};
        visited.add(start);
        int surface = 0;
        while (!todo.isEmpty()) {
            Point3D next = todo.pop();
            for (Point3D adj : adjacentPoints(next)) {
                if (!isInBounds(adj)) {
                    if (points.contains(adj)) {
                        throw new RuntimeException("out of bounds but still contained");
                    }
                } else if (points.contains(adj)) {
                    surface += 1;
                } else if (!visited.contains(adj)) {
                    visited.add(adj);
                    todo.add(adj);
                }
            }
        }
        return surface;
    }
}
