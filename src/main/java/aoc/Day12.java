package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Day12 {

    List<List<Integer>> map = new ArrayList<>();
    HashMap<Point, Integer> costs;
    HashMap<Point, Point> previous;
    HashSet<Point> visited;
    private Point position = null;
    private Point start = null;

    Day12(String filename) throws IOException {
        this.costs = new HashMap<>();
        this.previous = new HashMap<>();
        this.visited = new HashSet<>();

        List<String> lines = Files.readAllLines(Paths.get(filename));
        for (int y = 0; y < lines.size(); y++) {
            List<Integer> row = new ArrayList<>();
            for (int x = 0; x < lines.get(y).length(); x++) {
                char ch = lines.get(y).charAt(x);
                if (ch == 'S') {
                    this.start = new Point(x, y);
                    ch = 'a';
                } else if (ch == 'E') {
                    this.position = new Point(x, y);
                    ch = 'z';
                }
                row.add(ch - 'a');
                this.previous.put(new Point(x, y), null);
            }
            this.map.add(row);
        }
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/12.txt";
        Day12 solver = new Day12(filename);
        solver.solve();
        int p1 = solver.getCost(solver.start);
        System.out.printf("p1: %d\n", p1);
        int p2 = solver.getLowestA();
        System.out.printf("p2: %d\n", p2);
    }

    void solve() {
        this.costs.put(position, 0);
        Point current;
        while (true) {
            current = nextMinDistance();
            if (current == null) {
                break;
            }
            this.visited.add(current);
            int currentCost = costs.get(current);
            for (Point adj : getAdjacent(current)) {
                int cost = currentCost + 1;
                if (!costs.containsKey(adj) || cost < costs.get(adj)) {
                    costs.put(adj, cost);
                    previous.put(adj, current);
                }
            }
        }
    }

    // smallest point in costs that was not yet visited
    Point nextMinDistance() {
        Point min = null;
        int minCost = 0;
        for (Map.Entry<Point, Integer> set : costs.entrySet()) {
            Integer cost = set.getValue();
            if (cost != null && !visited.contains(set.getKey()) && (min == null || cost < minCost)) {
                min = set.getKey();
                minCost = cost;
            }
        }
        return min;
    }

    ArrayList<Point> getAdjacent(Point p) {
        ArrayList<Point> adj = new ArrayList<>() {{
            add(new Point(p.x+1, p.y));
            add(new Point(p.x-1, p.y));
            add(new Point(p.x, p.y+1));
            add(new Point(p.x, p.y-1));
        }};
        int height = getHeight(p);
        return adj.stream()
                .filter(this::boundsCheck)
                .filter(a -> !visited.contains(a))
                // at most one lower
                // z -> y :: getHeight(adj) - getHeight(p) = -1
                // x -> z :: getHeight(adj) - getHeight(p) = 2
                .filter(a -> getHeight(a) - height >= -1)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    boolean boundsCheck(int x, int y) {
        return x >= 0 && y >= 0 && y < map.size() && x < map.get(y).size();
    }

    boolean boundsCheck(Point p) {
        return boundsCheck(p.x, p.y);
    }

    Integer getHeight(int x, int y) {
        if (!boundsCheck(x, y)) {
            throw new IndexOutOfBoundsException();
        }
        return map.get(y).get(x);
    }

    Integer getHeight(Point p) {
        return getHeight(p.x, p.y);
    }

    Integer getCost(Point p) {
        return costs.get(p);
    }

    Integer getCost(int x, int y) {
        return getCost(new Point(x, y));
    }

    void printCosts() {
        for (int y = 0; y < map.size(); y++) {
            for (int x = 0; x < map.get(y).size(); x++ ) {
                if (getCost(x, y) == null) {
                    System.out.printf(" N%d", getHeight(x, y));
                } else {
                    System.out.printf("%3d", getHeight(x, y));
                }
            }
            System.out.println();
        }
    }

    Integer getLowestA() {
        Integer min = null;
        for (int y = 0; y < map.size(); y++) {
            for (int x = 0; x < map.get(y).size(); x++) {
                if (getHeight(x, y) == 0) {
                    Integer cost = getCost(x, y);
                    if (cost != null && (min == null || cost < min)) {
                        min = cost;
                    }
                }
            }
        }
        return min;
    }
}
