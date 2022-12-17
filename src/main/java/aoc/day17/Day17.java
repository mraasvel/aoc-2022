package aoc.day17;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

class Point {
    public long x;
    public long y;

    public Point(long x, long y) {
        this.x = x;
        this.y = y;
    }

    public Point add(Point other) {
        return new Point(this.x + other.x, this.y + other.y);
    }

    @Override
    public String toString() {
        return String.format("(%d, %d)", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        final long prime = 31;
        long result = 1;
        result = prime * result + x;
        result = prime * result + y;
        return (int)result;
    }
}


class Rock {
    ArrayList<Point> points;

    Rock(ArrayList<Point> points) {
        this.points = points;
    }

    // true if move was possible and happened
    // updates grid as appropriate
    Rock move(Point delta) {
        return new Rock(
                points
                        .stream()
                        .map(point -> point.add(delta))
                        .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    @Override
    public String toString() {
        return String.format("Rock: %s", points.toString());
    }
}

class Grid {
    HashSet<Point> points;
    long highestPoint;
    int width;

    Grid() {
        this.width = 7;
        this.highestPoint = 0; // floor
        this.points = new HashSet<>();
    }

    // call after rock is stationary
    void addRock(Rock rock) {
        for (Point point: rock.points) {
            long height = point.y + 1;
            highestPoint = Math.max(height, highestPoint);
            points.add(point);
        }
    }

    boolean canPlace(Rock rock) {
        for (Point point : rock.points) {
            if (points.contains(point) || point.y < 0 || point.x < 0 || point.x >= width) {
                return false;
            }
        }
        return true;
    }
}

public class Day17 {
    static private final Point LEFT = new Point(-1, 0);
    static private final Point RIGHT = new Point(1, 0);
    static private final Point DOWN = new Point(0, -1);
    static private final Point UP = new Point(0, 1);
    Grid grid = new Grid();
    long numRocks = 0;
    Rock currentRock = null;

    public static void main(String[] args) throws IOException {
        String filename = "inputs/17.txt";
        byte[] content = Files.readAllBytes(Paths.get(filename));
        Day17 solver = new Day17();
        long p1 = solver.solve(content, 2023);
        System.out.println(p1);
    }


    // Consideration: left most point has X = 0, lowest point has Y = 0
    // This way we can do y += highestPoint + 3 and x += 2
    // 0 -> (TL = (0, 0)
    // ####

    // 1 -> (L = (0, 1)), (B = (1, 0)
    // .#.
    // ###
    // .#.

    // 2 -> L = (0, 0)
    // ..#
    // ..#
    // ###

    // 3 -> B = (0, 0)
    // #
    // #
    // #
    // #

    // ##
    // ##
    ArrayList<Point> getNewRockPoints() {
        ArrayList<Point> points = new ArrayList<>();
        int type = (int)(numRocks % 5);
        if (type == 0) {
            for (int x = 0; x < 4; x++) {
                points.add(new Point(x, 0));
            }
        } else if (type == 1) {
            points.add(new Point(0, 1));
            points.add(new Point(1, 1));
            points.add(new Point(2, 1));
            points.add(new Point(1, 0));
            points.add(new Point(1, 2));
        } else if (type == 2) {
            for (int x = 0; x < 3; x++) {
                points.add(new Point(x, 0));
            }
            points.add(new Point(2, 1));
            points.add(new Point(2, 2));
        } else if (type == 3) {
            for (int y = 0; y < 4; y++) {
                points.add(new Point(0, y));
            }
        } else if (type == 4) {
            for (int x = 0; x < 2; x++) {
                for (int y = 0; y < 2; y++) {
                    points.add(new Point(x, y));
                }
            }
        }
        return points;
    }

    Point getNewRockPosition() {
        return new Point(2, grid.highestPoint + 3);
    }

    void nextRock() {
        if (currentRock != null) {
            grid.addRock(currentRock);
        }
        currentRock = new Rock(getNewRockPoints());
        Point transformation = getNewRockPosition();
        currentRock = currentRock.move(transformation);
        numRocks++;
        if (numRocks % 1000000 == 0) {
            System.out.printf("PROCESSED: %d\n", numRocks);
        }
    }

    // move rock if possible
    // returns false and doesn't modify currentRock if not possible
    boolean moveRock(Point delta) {
        Rock next = currentRock.move(delta);
        if (grid.canPlace(next)) {
            currentRock = next;
            return true;
        }
        return false;
    }

    long solve(byte[] content, long maxRocks) {
        nextRock();
        int contentIndex = 0;
        while (numRocks < maxRocks) {
           byte direction = content[contentIndex % content.length];
           if (direction == '>') {
                moveRock(RIGHT);
           } else if (direction == '<') {
               moveRock(LEFT);
           }
           if (!moveRock(DOWN)) {
               // cannot move down so the rock rests
               nextRock();
           }
           contentIndex++;
       }
       return grid.highestPoint;
    }
}
