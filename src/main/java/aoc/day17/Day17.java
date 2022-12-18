package aoc.day17;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class UniqueState {
    static private final Point LEFT = new Point(-1, 0);
    static private final Point RIGHT = new Point(1, 0);
    static private final Point DOWN = new Point(0, -1);
    static private final Point[][] SHAPES = {
            { new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(3, 0) },
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 0), new Point(1, 2) },
            { new Point(0, 0), new Point(1, 0), new Point(2, 0), new Point(2, 1), new Point(2, 2) },
            { new Point(0, 0), new Point(0, 1), new Point(0, 2), new Point(0, 3) },
            { new Point(0, 0), new Point(1, 0), new Point(0, 1), new Point(1, 1) },
    };
    int directionIndex;
    long numRocks;
    Grid grid;
    byte[] directions;
    Rock rock = null;

    UniqueState(long numRocks, int directionIndex, Grid grid, byte[] directions) {
        this.numRocks = numRocks;
        this.directionIndex = directionIndex;
        this.grid = new Grid(grid);
        this.directions = directions;
    }

    static UniqueState defaultState(byte[] directions) {
        return new UniqueState(0, 0, new Grid(), directions);
    }

    // generate the next state with the next rock placed
    static UniqueState f(UniqueState state, byte[] directions) {
        UniqueState next = new UniqueState(state.numRocks, state.directionIndex, state.grid, directions);
        next.rock = next.nextRock();
        do {
            switch (next.nextDirection(directions)) {
                case '>' -> next.moveRock(RIGHT);
                case '<' -> next.moveRock(LEFT);
            }
        } while (next.moveRock(DOWN));
        next.rest();
        return next;
    }

    static private List<Point> getNewRockPoints(int type) {
        return Arrays.stream(SHAPES[type]).collect(Collectors.toCollection(ArrayList::new));
    }

    Point getNewRockPosition() {
        return new Point(2, getHighestPoint() + 3);
    }

    byte nextDirection(byte[] directions) {
        byte dir = directions[directionIndex];
        directionIndex = (directionIndex + 1) % directions.length;
        return dir;
    }

    Rock nextRock() {
        Rock rock = new Rock(getNewRockPoints(rockType()));
        numRocks += 1;
        Point transformation = getNewRockPosition();
        return rock.move(transformation);
    }

    // move rock if possible
    // returns false and doesn't modify currentRock if not possible
    boolean moveRock(Point delta) {
        Rock next = rock.move(delta);
        if (grid.canPlace(next)) {
            rock = next;
            return true;
        }
        return false;
    }

    UniqueState next() {
        return UniqueState.f(this, this.directions);
    }

    void rest() {
        grid.addRock(rock);
        rock = null;
    }

    int rockType() {
        return (int)(this.numRocks % 5);
    }

    public boolean equals(UniqueState other) {
        return this.directionIndex == other.directionIndex
                && rockType() == other.rockType()
                && grid.hasSameShape(other.grid);
    }

    public long getHighestPoint() {
        return grid.highestPoint;
    }
}

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
    List<Point> points;

    Rock(List<Point> points) {
        this.points = points;
    }

    // apply delta to all points that make up the rock
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
        // add floor points
        for (int x = 0; x < width; x++) {
            points.add(new Point(x, -1));
        }
    }

    Grid(Grid other) {
        this.width = other.width;
        this.highestPoint = other.highestPoint;
        this.points = new HashSet<>(other.points);
    }

    // call after rock is stationary
    void addRock(Rock rock) {
        for (Point point: rock.points) {
            long height = point.y + 1;
            highestPoint = Math.max(height, highestPoint);
            points.add(point);
        }
        filterPoints();
    }

    // remove points that are not relevant, aka points that are not reachable from above
    void filterPoints() {
        // highestPoint as Y will be one row above the highest point since it's the height of the tower
        Point current = new Point(0, highestPoint);
        Stack<Point> todo = new Stack<>(){{ add(current); }};
        HashSet<Point> visited = new HashSet<>();
        while (!todo.isEmpty()) {
            Point next = todo.pop();
            visited.add(next);
            if (points.contains(next)) {
                continue;
            }
            addAdjacent(next, visited, todo);
        }
        // set intersection of points (blocks) and visited (reachable) nodes
        points.retainAll(visited);
    }

    void addAdjacent(Point point, HashSet<Point> visited, Stack<Point> todo) {
        ArrayList<Point> adjacent = new ArrayList<>(){{
            add(new Point(point.x - 1, point.y));
            add(new Point(point.x + 1, point.y));
            add(new Point(point.x, point.y - 1));
        }};
        adjacent
                .stream()
                .filter(p -> !visited.contains(p))
                .filter(p -> p.x >= 0 && p.x < width)
                .forEach(todo::add);
    }

    boolean canPlace(Rock rock) {
        for (Point point : rock.points) {
            if (points.contains(point) || point.x < 0 || point.x >= width) {
                return false;
            }
        }
        return true;
    }

    boolean contains(Point point) {
        return this.points.contains(point);
    }

    // compare 'roof' shape of a grid
    boolean hasSameShape(Grid other) {
        if (points.size() != other.points.size()) {
            return false;
        }
        // transform points so that they have the same height as if they were in the other grid
        long deltaY = other.highestPoint - highestPoint;
        for (Point point : points) {
            if (!other.contains(new Point(point.x, point.y + deltaY))) {
                return false;
            }
        }
        return true;
    }
}

public class Day17 {

    public static void main(String[] args) throws IOException {
        String filename = "inputs/17.txt";
        byte[] content = Files.readAllBytes(Paths.get(filename));
        Day17 solver = new Day17();
        long p1 = solver.partOne(content, 2022);
        System.out.printf("P1: %d\n", p1);

        long p2 = solver.partTwo(UniqueState.defaultState(content), 1000000000000L);
        System.out.println(p2);
    }

    long partOne(byte[] content, long maxRocks) {
        UniqueState state = UniqueState.defaultState(content);
        while (state.numRocks < maxRocks) {
            state = state.next();
        }
        return state.getHighestPoint();
    }

    long computeHeight(long cycleHeightChange, long cycleStart, long cycleDuration, long maxRocks, UniqueState cycleStartState) {
        long numCycles = (maxRocks - cycleStart) / cycleDuration;
        System.out.printf("number of cycles: %d\n", numCycles);
        long remainingIterations = (maxRocks - cycleStart) % cycleDuration;
        System.out.printf("remaining iterations in cycle: %d\n", remainingIterations);
        while (remainingIterations > 0) {
            cycleStartState = cycleStartState.next();
            remainingIterations--;
        }
        return cycleStartState.getHighestPoint() + cycleHeightChange * numCycles;
    }

    // cycle detection
    long partTwo(UniqueState initial, long maxRocks) {
        UniqueState tortoise = initial.next();
        UniqueState hare = initial.next().next();
        while (!tortoise.equals(hare)) {
            tortoise = tortoise.next();
            hare = hare.next().next();
        }

        // mu = position of first repetition
        int mu = 0;
        tortoise = initial;
        while (!tortoise.equals(hare)) {
            tortoise = tortoise.next();
            hare = hare.next();
            mu += 1;
        }

        // lambda = length of the cycle
        int lambda = 1;
        hare = tortoise.next();
        while (!tortoise.equals(hare)) {
            hare = hare.next();
            lambda += 1;
        }
        // the cycle's change in height is hare.height() - tortoise.height();
        // the cycle's change in numRocks will be equal to lambda
        long cycleHeightChange = hare.getHighestPoint() - tortoise.getHighestPoint();
        return computeHeight(cycleHeightChange, mu, lambda, maxRocks, tortoise);
    }
}
