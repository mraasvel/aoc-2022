package aoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

interface Operation2 {
    static Operation2 operationFactory(String s) {
        if (s.equals("+")) {
            return new Plus();
        } else if (s.equals("*")) {
            return new Multiply();
        } else {
            throw new IllegalArgumentException();
        }
    }
    long execute(Operand a, Operand b, long oldValue);
}

class Plus implements Operation2 {
    public long execute(Operand a, Operand b, long oldValue) {
        return a.getValue(oldValue) + (b.getValue(oldValue));
    }

    @Override
    public String toString() {
        return "+";
    }
}

class Multiply implements Operation2 {
    public long execute(Operand a, Operand b, long oldValue) {
        return a.getValue(oldValue) * (b.getValue(oldValue));
    }

    @Override
    public String toString() {
        return "*";
    }
}

interface Operand {
    static Operand operandFactory(String x) {
        if (x.equals("old")) {
            return new Variable();
        } else {
            return new Number(Long.parseLong(x));
        }
    }
    long getValue(long oldValue);
}

class Variable implements Operand {
    public long getValue(long oldValue) {
        return oldValue;
    }

    @Override
    public String toString() {
        return "old";
    }
}

class Number implements Operand {
    private final long n;
    Number(long n) {
        this.n = n;
    }
    public long getValue(long oldValue) {
        return n;
    }

    @Override
    public String toString() {
        return Long.toString(n);
    }
}

class Expression {
    Operand a;
    Operand b;
    Operation2 op;

    Expression(Operand a, Operand b, Operation2 op) {
        this.a = a;
        this.b = b;
        this.op = op;
    }

    static Expression parseOperation(String line) {
        Matcher matcher = Pattern.compile("new =\\s*(\\S+)\\s*(.)\\s*(\\S+)$").matcher(line);
        matcher.find();

        Operand a = Operand.operandFactory(matcher.group(1));
        Operand b = Operand.operandFactory(matcher.group(3));
        Operation2 op = Operation2.operationFactory(matcher.group(2));
        return new Expression(a, b, op);
    }

    public long computeValue(long oldValue) {
        return op.execute(a, b, oldValue);
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", a.toString(), op.toString(), b.toString());
    }
}

class Monkey {
    private final ArrayDeque<Long> items = new ArrayDeque<>();
    private Expression operation;
    private final long divisibleBy;
    private final long trueMonkey;
    private final long falseMonkey;
    private long itemsInspected = 0;

    Monkey(String[] data) {
        parseItems(data[1]);
        parseOperation(data[2]);
        divisibleBy = parseNumber(data[3]);
        trueMonkey = parseNumber(data[4]);
        falseMonkey = parseNumber(data[5]);
    }

    void parseItems(String line) {
        Matcher matcher = Pattern.compile("(\\d+)").matcher(line);
        while (matcher.find()) {
            addItem(Long.parseLong(matcher.group(1)));
        }
    }

    void parseOperation(String line) {
        this.operation = Expression.parseOperation(line);
    }

    long parseNumber(String line) {
        Matcher matcher = Pattern.compile("(\\d+)").matcher(line);
        matcher.find();
        return Long.parseLong(matcher.group(0));
    }

    void addItem(long x) {
        items.add(x);
    }

    long peek() {
        return items.peekFirst();
    }

    long pop() {
        return items.removeFirst();
    }

    boolean hasItem() {
        return !items.isEmpty();
    }

    private void updateTop(long newValue) {
        this.items.pop();
        this.items.addFirst(newValue);
    }

    void inspectItem() {
        this.itemsInspected += 1;
        long oldValue = this.peek();
        long newValue = this.operation.computeValue(oldValue) / 3;
        this.updateTop(newValue);
    }

    private boolean test() {
        return peek() % divisibleBy == 0;
    }

    int nextMonkey() {
        if (test()) {
            return (int)trueMonkey;
        } else {
            return (int)falseMonkey;
        }
    }

    long getItemsInspected() {
        return itemsInspected;
    }
}

public class Day11 {
    ArrayList<Monkey> monkeys = null;
    Day11(String filename) {
        try {
            String[] input = Files.readString(Paths.get(filename)).split("\n\n");
            monkeys = Arrays.stream(input).map(x -> x.split("\n")).map(Monkey::new).collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    void doRound() {
        for (Monkey monkey: monkeys) {
            while (monkey.hasItem()) {
                monkey.inspectItem();
                int nextMonkey = monkey.nextMonkey();
                long item = monkey.pop();
                monkeys.get(nextMonkey).addItem(item);
            }
        }
    }

    long partOne() {
        for (int i = 0; i < 20; i++) {
            doRound();
        }
        monkeys.sort((Monkey a, Monkey b) -> Long.compare(b.getItemsInspected(), a.getItemsInspected()));
        return monkeys.get(0).getItemsInspected() * monkeys.get(1).getItemsInspected();
    }

    public static void main(String[] args) {
        String filename = "inputs/ex.txt";
        Day11 day11 = new Day11(filename);
        long p1 = day11.partOne();
        System.out.printf("p1: %d\n", p1);
    }
}
