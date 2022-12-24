package aoc.day24;

import aoc.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class Blizzard {
    static final Point RIGHT = new Point(1, 0);
    static final Point LEFT = new Point(-1, 0);
    static final Point DOWN = new Point(0, 1);
    static final Point UP = new Point(0, -1);
    private final Point direction;
    private final Point position;
    Blizzard(Point position, Point direction) {
        this.position = position;
        this.direction = direction;
    }

    static Point charToDirection(char ch) {
        return switch (ch) {
            case '>' -> RIGHT;
            case '<' -> LEFT;
            case '^' -> UP;
            case 'v' -> DOWN;
            default -> throw new RuntimeException("invalid direction char");
        };
    }

    // ###
    // #.#
    // ###
    // (1,1)
    // width = 3, height = 3
    // inefficient but t^2 is not that bad, if it's a bottleneck we fix it
    Point doMove(int time, int width, int height) {
        Point position = new Point(this.position);
        for (int i = 0; i < time; i++) {
            position = position.add(direction);
            if (position.x == 0) {
                position.x = width - 2;
            } else if (position.x == width - 1) {
                position.x = 1;
            } else if (position.y == 0) {
                position.y = height - 2;
            } else if (position.y == height - 1) {
                position.y = 1;
            }
        }
        return position;
    }

    @Override
    public String toString() {
        return String.format("Blizzard[pos=%s,dir=%s]", position, direction);
    }
}

class BlizzardTracker {
    int width;
    int height;
    private final List<Blizzard> blizzards;
    // memoized time/steps -> blizzard positions
    private final Map<Integer, Set<Point>> blizzardTiles = new HashMap<>();

    BlizzardTracker(List<Blizzard> blizzards, int width, int height) {
        this.blizzards = blizzards;
        this.width = width;
        this.height = height;
    }

    Set<Point> blizzardsAtTime(int time) {
        if (blizzardTiles.containsKey(time)) {
            return blizzardTiles.get(time);
        }
        Set<Point> taken = blizzards.stream().map(blizzard -> blizzard.doMove(time, width, height)).collect(Collectors.toSet());
        this.blizzardTiles.put(time, taken);
        return taken;
    }
}

class State {
    static final List<Point> DELTAS = new ArrayList<>(){{
        add(new Point(Blizzard.LEFT));
        add(new Point(Blizzard.RIGHT));
        add(new Point(Blizzard.UP));
        add(new Point(Blizzard.DOWN));
        add(new Point(0, 0)); // wait
    }};
    Point position;
    int steps;

    State(Point position, int steps) {
        this.position = position;
        this.steps = steps;
    }

    static boolean isInBounds(Point point, int width, int height) {
        return (point.x > 0 && point.y > 0 && point.x < width - 1 && point.y < height - 1)
                || (point.x == 1 && point.y == 0) || (point.x == width - 2 && point.y == height - 1);
    }

    List<State> expand(BlizzardTracker blizzardTracker, int width, int height, Set<State> handled) {
        int nextSteps = steps + 1;
        Set<Point> forbidden = blizzardTracker.blizzardsAtTime(nextSteps);
        return DELTAS
                .stream()
                .map(delta -> position.add(delta))
                .filter(point -> isInBounds(point, width, height))
                .filter(point -> !forbidden.contains(point))
                .map(point -> new State(point, nextSteps))
                .filter(state -> !handled.contains(state))
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        State other = (State) otherObject;
        return Objects.equals(position, other.position) && steps == other.steps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, steps);
    }

    @Override
    public String toString() {
        return String.format("State[pos=%s, steps=%d]", position, steps);
    }
}

public class Day24 {
    BlizzardTracker blizzardTracker;
    int width;
    int height;
    Point start;
    Point end;

    Day24(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));
        List<Blizzard> blizzards = new ArrayList<>();
        for (int y = 0; y < lines.size(); y++) {
            byte[] line = lines.get(y).getBytes();
            for (int x = 0; x < line.length; x++) {
                if (line[x] == '.') {
                    if (start == null) {
                        start = new Point(x, y);
                    }
                    end = new Point(x, y);
                }
                if (line[x] == '.' || line[x] == '#') {
                    continue;
                }
                Blizzard blizzard = new Blizzard(new Point(x, y), Blizzard.charToDirection((char)line[x]));
                blizzards.add(blizzard);
            }
        }
        width = lines.get(0).length();
        height = lines.size();
        this.blizzardTracker = new BlizzardTracker(blizzards, width, height);
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/24.txt";
        Day24 solver = new Day24(filename);
        int p1 = solver.partOne();
        System.out.println(p1);
        int p2 = solver.partTwo();
        System.out.println(p2);
    }

    int bfs(State initial, Point goal) {
        Set<State> handled = new HashSet<>();
        Queue<State> queue = new ArrayDeque<>(){{
            add(initial);
        }};
        int maxQueue = 0;
        while (!queue.isEmpty()) {
            maxQueue = Math.max(maxQueue, queue.size());
            State top = queue.remove();
            if (top.position.equals(goal)) {
                return top.steps;
            }
            List<State> nextStates = top.expand(blizzardTracker, width, height, handled);
            queue.addAll(nextStates);
            handled.addAll(nextStates);
        }
        throw new RuntimeException("no solution found");
    }

    int partOne() {
        return bfs(new State(start, 0), end);
    }

    int partTwo() {
        // first bfs to end, then from end to start, then from that start to the end again
        int steps = partOne();
        int toStart = bfs(new State(end, steps), start);
        return bfs(new State(start, toStart), end);
    }
}
