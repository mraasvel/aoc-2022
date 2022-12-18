package aoc;


import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InaccessibleObjectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Valve {
    int flowRate;
    Map<String, Integer> distances;
    ArrayList<String> connections;
    String id;

    Valve(String id, int flowRate, ArrayList<String> connections) {
        this.id = id;
        this.flowRate = flowRate;
        this.connections = connections;
        this.distances = new HashMap<>();
        distances.put(id, 0);
    }

    // Valve HH has flow rate=12; tunnels lead to valves RQ, NL, ZQ
    static Valve fromLine(String line) {
        Matcher matcher = Pattern.compile("Valve ([A-Z]{2}) has flow rate=(\\d+); tunnels? leads? to valves? (.*$)").matcher(line);
        if (!matcher.find()) {
            throw new RuntimeException(String.format("no match on %s", line));
        }

        String id = matcher.group(1);
        int flowRate = Integer.parseInt(matcher.group(2));
        matcher = Pattern.compile("([A-Z]{2})").matcher(matcher.group(3));
        ArrayList<String> connections = new ArrayList<>();
        while (matcher.find()) {
            connections.add(matcher.group(1));
        }
        return new Valve(id, flowRate, connections);
    }

    public ArrayList<String> getConnections() {
        return this.connections;
    }

    public Map<String, Integer> getDistances() {
        return this.distances;
    }

    public String getId() {
        return id;
    }

    public int getFlowRate() {
        return flowRate;
    }

    private void doComputeDistances(Map<String, Valve> graph, ArrayList<String> connections, int distance) {
        for (String connection: connections) {
            if (distances.containsKey(connection)) {
                int knownDistance = distances.get(connection);
                if (distance >= knownDistance) {
                    continue;
                }
            }
            distances.put(connection, distance);
            doComputeDistances(graph, graph.get(connection).getConnections(), distance + 1);
        }
    }

    void computeDistances(Map<String, Valve> graph) {
        doComputeDistances(graph, connections, 1);
    }

    @Override
    public String toString() {
        return String.format("{ V(%s): FR(%d), Connections: %s, Distances: %s }", id, flowRate, connections.toString(), distances.toString());
    }
}

class State {
    Map<String, Valve> graph;
    Set<String> opened;
    int pressureReleased;
    int minutes;
    private int timeLimit;
    String position;
    static State initialState(Map<String, Valve> graph, int timeLimit) {
        return new State(
                graph,
                new HashSet<>(),
                1,
                0,
                "AA",
                timeLimit
        );
    }

    State(Map<String, Valve> graph, Set<String> opened, int minutes, int pressureReleased, String position, int timeLimit) {
        this.graph = graph;
        this.minutes = minutes;
        this.pressureReleased = pressureReleased;
        this.position = position;
        this.opened = opened;
        this.timeLimit = timeLimit;
    }

    public int getPressure() {
        return pressureReleased;
    }

    List<State> expand() {
        Valve current = graph.get(position);
        List<State> states = new ArrayList<>();
        graph.entrySet().stream().filter(entry -> !opened.contains(entry.getKey())).forEach(entry -> {
            Valve valve = entry.getValue();
            int timeToOpen = current.getDistances().get(valve.getId()) + 1;
            if (minutes + timeToOpen > timeLimit || valve.getFlowRate() == 0) {
                return;
            }
            HashSet<String> newOpened = new HashSet<>(opened);
            newOpened.add(valve.getId());
            int newMinutes = minutes + timeToOpen;
            int newPressure = pressureReleased + (timeLimit - (newMinutes - 1)) * valve.getFlowRate();
            states.add(new State(graph, newOpened, newMinutes, newPressure, valve.getId(), timeLimit));
        });
        return states;
    }
}

class Worker {
    int timeLimit;
    int elapsedTime;
    Valve position;

    Worker(Valve position, int elapsedTime, int timeLimit) {
        this.position = position;
        this.elapsedTime = elapsedTime;
        this.timeLimit = timeLimit;
    }

    List<ChangeInState> getChanges(Map<String, Valve> graph, Set<Valve> opened) {
        List<ChangeInState> changes = graph.entrySet()
                .stream()
                .filter(entry -> !opened.contains(entry.getKey()))
                .filter(entry -> entry.getValue().getFlowRate() > 0)
                .map(entry -> {
                    int distance = position.getDistances().get(entry.getKey());
                    int timeToOpen = distance + 1;
                    int newElapsed = elapsedTime + timeToOpen;
                    if (newElapsed > timeLimit) {
                        // cannot visit this node in the time remaining
                        return null;
                    }
                    Valve dest = entry.getValue();
                    int deltaPressure = (timeLimit - elapsedTime) * dest.getFlowRate();
                    return new ChangeInState(new Worker(dest, newElapsed, timeLimit), deltaPressure, true);
                })
                .filter(v -> v != null)
                .collect(Collectors.toCollection(ArrayList::new));
        if (changes.isEmpty()) {
            changes.add(new ChangeInState(this, 0, false));
        }
        return changes;
    }

    @Override
    public String toString() {
        return String.format("Worker at(%s), elapsed(%d)", position.getId(), elapsedTime);
    }
}

class ChangeInState {
    Worker newWorker;
    int deltaPressure;
    boolean isChange;
    ChangeInState(Worker newWorker, int deltaPressure, boolean isChange) {
        this.newWorker = newWorker;
        this.deltaPressure = deltaPressure;
        this.isChange = isChange;
    }

    // pair is fine if either actually changes something and they don't both have the same destination
    static boolean isValidPair(ChangeInState a, ChangeInState b) {
        return (a.isChange || b.isChange) && a.newWorker.position != b.newWorker.position;
    }
}

class CaveState {
    Set<Valve> opened;
    Map<String, Valve> graph;
    List<Worker> workers;
    int pressure;

    CaveState(Map<String, Valve> graph, int timeLimit) {
        this.graph = graph;
        this.pressure = 0;
        this.opened = new HashSet<>();
        Valve position = graph.get("AA");
        workers = new ArrayList<>(){{
            add(new Worker(position, 1, timeLimit));
            add(new Worker(position, 1, timeLimit));
        }};
    }

    CaveState(Map<String, Valve> graph, Set<Valve> opened, List<Worker> workers) {
        this.graph = graph;
        this.opened = opened;
        this.workers = workers;
    }

    CaveState applyChanges(List<ChangeInState> changes) {
        CaveState next = new CaveState(graph, new HashSet<>(opened), new ArrayList<>());
        changes.stream().forEach(change -> {
            next.addWorker(change.newWorker);
            next.addPressure(change.deltaPressure);
            next.openPosition(change.newWorker.position);
        });
        return next;
    }

    public void addWorker(Worker worker) {
        this.workers.add(worker);
    }

    public int getPressure() {
        return this.pressure;
    }

    public void addPressure(int delta) {
        this.pressure += delta;
    }

    public void openPosition(Valve position) {
        this.opened.add(position);
    }

    List<CaveState> expand() {
        List<CaveState> states = new ArrayList<>();
        List<List<ChangeInState>> changes = workers.stream().map(worker -> worker.getChanges(graph, opened)).collect(Collectors.toCollection(ArrayList::new));
        // for all valid pairs of changes, apply the changes to generate a new state
        // a valid pair would be one where the destination is different and one of the pairs actually changes something
        for (int i = 0; i < changes.get(0).size(); i++) {
            for (int j = 0; j < changes.get(1).size(); j++) {
                ChangeInState a = changes.get(0).get(i);
                ChangeInState b = changes.get(1).get(j);
                if (!ChangeInState.isValidPair(a, b)) {
                    continue;
                }
                states.add(applyChanges(new ArrayList<>(){{
                    add(a);
                    add(b);
                }}));
            }
        }
        return states;
    }

    @Override
    public String toString() {
        return String.format("Pressure(%d), Workers(%s), Opened(%s)", this.pressure, this.workers, this.opened);
    }
}

public class Day16 {

    Map<String, Valve> graph;

    Day16(String filename) throws IOException {
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            this.graph = lines.map(Valve::fromLine).collect(Collectors.toMap(Valve::getId, valve -> valve));
            graph.entrySet().stream().forEach(entry -> {
                entry.getValue().computeDistances(graph);
            });
        }
    }

    int partOne() {
        Stack<State> states = new Stack<>(){{ add(State.initialState(graph, 30)); }};
        State bestSoFar = null;
        while (!states.isEmpty()) {
            State next = states.pop();
            if (bestSoFar == null || next.getPressure() > bestSoFar.getPressure()) {
                bestSoFar = next;
            }
            states.addAll(next.expand());
        }
        return bestSoFar.getPressure();
    }

    int partTwo() {
        Stack<CaveState> states = new Stack<>(){{
            add(new CaveState(graph, 26));
        }};
        CaveState bestSoFar = null;
        while (!states.isEmpty()) {
            CaveState next = states.pop();
            if (bestSoFar == null || next.getPressure() > bestSoFar.getPressure()) {
                bestSoFar = next;
                System.out.println(bestSoFar.getPressure());
            }
            states.addAll(next.expand());
        }
        return bestSoFar.getPressure();
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/16.txt";
        Day16 solver = new Day16(filename);
        int p1 = solver.partOne();
        System.out.println(p1);
        int p2 = solver.partTwo();
        System.out.println(p2);
    }
}
