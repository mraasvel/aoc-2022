package aoc.day19;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// utility type to store costs, income, etc
class Minerals {
    enum Type {
        Ore,
        Clay,
        Obsidian,
        Geode
    }

    Map<Type, Integer> values = new HashMap<>();

    Minerals() {
        for (Type type: Type.values()) {
            values.put(type, 0);
        }
    }

    public void set(Type type, int amount) {
        this.values.put(type, amount);
    }

    public int get(Type type) {
        return values.getOrDefault(type, 0);
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        Minerals other = (Minerals) otherObject;
        return Objects.equals(values, other.values);
    }
    @Override
    public String toString() {
        return String.format("Minerals[values=%s]", values.toString());
    }
}

class Blueprint {
    int id;
    // each robot has a Minerals type which has a collection of costs
    Map<Minerals.Type, Minerals> robotCosts;
    // maximum number of income needed to build anything every turn
    Minerals maximumIncome = new Minerals();

    // INPUT
    // Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
    Blueprint(String line) {
        this.robotCosts = new HashMap<>();
        String[] parts = line.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException();
        }
        parseId(parts[0]);
        String[] costSplit = parts[1].split("\\.");
        parseCosts(Minerals.Type.Ore, costSplit[0]);
        parseCosts(Minerals.Type.Clay, costSplit[1]);
        parseCosts(Minerals.Type.Obsidian, costSplit[2]);
        parseCosts(Minerals.Type.Geode, costSplit[3]);
        for (Minerals.Type type: Minerals.Type.values()) {
            int max = calculateMaxCost(type);
            if (max == 0) {
                this.maximumIncome.set(type, Integer.MAX_VALUE);
            } else {
                this.maximumIncome.set(type, max);
            }
        }
    }

    private int calculateMaxCost(Minerals.Type type) {
        return robotCosts.values().stream().map(mineral -> mineral.get(type)).max(Integer::compare).get();
    }

    private void parseId(String s) {
        this.id = Integer.parseInt(s.split(" ")[1]);
    }

    private Minerals.Type parseOreType(String s) {
        return switch (s) {
            case "ore" -> Minerals.Type.Ore;
            case "clay" -> Minerals.Type.Clay;
            case "obsidian" -> Minerals.Type.Obsidian;
            default -> throw new RuntimeException(String.format("invalid ore type: %s", s));
        };
    }

    private void parseCosts(Minerals.Type type, String line) {
        Matcher matcher = Pattern.compile("(\\d+) (\\S+)").matcher(line);
        Minerals costs = new Minerals();
        while (matcher.find()) {
            int amount = Integer.parseInt(matcher.group(1));
            Minerals.Type oreType = parseOreType(matcher.group(2));
            costs.set(oreType, amount);
        }
        this.robotCosts.put(type, costs);
    }

    public Integer getMaxIncome(Minerals.Type type) {
        return maximumIncome.get(type);
    }

    public Minerals getRobotCost(Minerals.Type type) {
        return robotCosts.get(type);
    }

    @Override
    public String toString() {
        return String.format("Blueprint[id=%s, robotCosts=%s, maximumIncome=%s]", id, robotCosts.toString(), maximumIncome.toString());
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        Blueprint other = (Blueprint) otherObject;
        return id == other.id;
    }
}

class State {
    Blueprint blueprint;
    Map<Minerals.Type, Integer> bank;
    Map<Minerals.Type, Integer> income;
    int timeLeft;

    State(Blueprint blueprint, int timeLeft) {
        this.blueprint = blueprint;
        this.bank = new HashMap<>();
        this.income = new HashMap<>();
        this.timeLeft = timeLeft;
        for (Minerals.Type type: Minerals.Type.values()) {
            bank.put(type, 0);
            income.put(type, 0);
        }
    }

    void setBank(Map<Minerals.Type, Integer> bank) {
        this.bank = bank;
    }

    void setIncome(Map<Minerals.Type, Integer> income) {
        this.income = income;
    }

    void addToIncome(Minerals.Type type, int amount) {
        int current = income.getOrDefault(type, 0);
        income.put(type, amount + current);
    }

    void addToBank(Minerals.Type type, int amount) {
        int current = bank.getOrDefault(type, 0);
        bank.put(type, amount + current);
    }

    // -1 if we cannot build the robot
    // >= 0 representing the time it will take to build the robot
    int timeToBuild(Minerals robotCost) {
        int time = 0;
        for (Map.Entry<Minerals.Type, Integer> entry : robotCost.values.entrySet()) {
            int current = bank.getOrDefault(entry.getKey(), 0);
            int in = income.getOrDefault(entry.getKey(), 0);
            int cost = Math.max(0, entry.getValue() - current);
            if (in == 0 && cost > 0) {
                // cannot build
                return -1;
            }
            // ceil division. 5 / 2 => 3, 4 / 2 => 2
            int timeCost = (int) Math.ceil((double)cost / in) + 1;
            if (timeCost > timeLeft) {
                return -1;
            }
            time = Math.max(time, timeCost);
        }
        return time;
    }

    State buildRobot(int timeNeeded, Minerals.Type type) {
        // decrease timeLeft by timeNeeded
        State newState = new State(blueprint, timeLeft - timeNeeded);
        newState.setBank(new HashMap<>(bank));
        newState.setIncome(new HashMap<>(income));

        // update income from new robot
        newState.addToIncome(type, 1);

        // update bank with all generated income
        for (Map.Entry<Minerals.Type, Integer> entry : income.entrySet()) {
            int amount = entry.getValue() * timeNeeded;
            newState.addToBank(entry.getKey(), amount);
        }

        // update bank, subtract all costs needed to build the robot
        for (Map.Entry<Minerals.Type, Integer> entry : blueprint.getRobotCost(type).values.entrySet()) {
            int amount = -1 * entry.getValue();
            newState.addToBank(entry.getKey(), amount);
        }
        return newState;
    }

    public int getGeodesAtEnd() {
        return bank.getOrDefault(Minerals.Type.Geode, 0)
                + income.getOrDefault(Minerals.Type.Geode, 0) * timeLeft;
    }

    private int summation(int n) {
        return n * (n + 1)  / 2;
    }

    public int getMaximumGeodes() {
        // if we build a geode every turn we get the maximum number of geodes in this state
        return getGeodesAtEnd() + summation(timeLeft - 1);
    }

    private boolean isMaxIncome(Minerals.Type type) {
        return blueprint.getMaxIncome(type).equals(income.getOrDefault(type, 0));
    }

    // go over each robot in the blueprint and calculate how much time we need to build it
    // create a state with each robot built that we can build.
    // build only geode if it is possible
    List<State> expand() {
        List<State> states = new ArrayList<>();
        for (Map.Entry<Minerals.Type, Minerals> entry : blueprint.robotCosts.entrySet()) {
            int timeNeeded = timeToBuild(entry.getValue());
            if (timeNeeded == -1 || timeNeeded > timeLeft) {
                continue;
            }
            // skip states that are useless, we don't build robots if we don't need more of that income type
            // because we can already afford all robots every turn
            if (isMaxIncome(entry.getKey())) {
                continue;
            }
            // we can reach this state
            // 1. increase income by 1 for robotType
            // 2. increase bank by income * timeNeeded
            // 3. decrease timeLeft by timeNeeded
            // 4. remove cost of robot from bank
            states.add(buildRobot(timeNeeded, entry.getKey()));
        }
        return states;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) return true;
        if (otherObject == null) return false;
        if (getClass() != otherObject.getClass()) return false;
        State other = (State) otherObject;
        return timeLeft == other.timeLeft
                && Objects.equals(blueprint, other.blueprint)
                && Objects.equals(bank, other.bank)
                && Objects.equals(income, other.income);
    }
    @Override
    public String toString() {
        return String.format("State [\nbank=%s,\nincome=%s,\ntimeLeft=%d,\nminutes=%d,\n]\n", bank, income, timeLeft, 24 - timeLeft);
    }
}

public class Day19 {
    List<Blueprint> blueprints;

    // main concept is we prune states by if the maximum number of possible geodes is not better than what we have found
    int maximumGeodesOpened(Blueprint blueprint, int time) {
        State initial = new State(blueprint, time);
        initial.addToIncome(Minerals.Type.Ore, 1);
        int geodes = 0;
        Stack<State> todo = new Stack<>(){{ add(initial); }};
        while (!todo.isEmpty()) {
            State next = todo.pop();
            geodes = Math.max(geodes, next.getGeodesAtEnd());
            // expansion
            for (State s : next.expand()) {
                if (s.getMaximumGeodes() <= geodes) {
                    continue;
                }
                todo.add(s);
            }
        }
        return geodes;
    }

    int partOne() {
        int total = 0;
        for (Blueprint bp : blueprints) {
            int id = bp.id;
            int maximumGeodes = maximumGeodesOpened(bp, 24);
            total = total + id * maximumGeodes;
        }
        return total;
    }

    int partTwo() {
        int total = 1;
        for (int i = 0; i < 3; i++) {
            Blueprint bp = blueprints.get(i);
            int maximumGeodes = maximumGeodesOpened(bp, 32);
            total *= maximumGeodes;
        }
        return total;
    }

    Day19(String filename) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            this.blueprints = lines.map(Blueprint::new).collect(Collectors.toCollection(ArrayList::new));
        }
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/19.txt";
        Day19 solver = new Day19(filename);
        int p1 = solver.partOne();
        System.out.println(p1);

        int p2 = solver.partTwo();
        System.out.println(p2);
    }
}
