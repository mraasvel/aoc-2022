package aoc.day21;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class Operation {
    char type;
    long value;
    boolean isLhs;
    Operation(char type, long value, boolean isLhs) {
        this.type = type;
        this.value = value;
        this.isLhs = isLhs;
    }

    @Override
    public String toString() {
        return String.format("Operation[type=%c,value=%d]", type, value);
    }
}

public class Day21 {
    Map<String, String> monkeys;
    Map<String, Long> values = new HashMap<>();

    // monkey -> command
    static Map<String, String> parse(String filename) throws IOException {
        Map<String, String> monkeys = new HashMap<>();
        List<String> lines = Files.readAllLines(Paths.get(filename));
        for (String line: lines) {
            String[] parts = line.split(": ");
            monkeys.put(parts[0], parts[1]);
        }
        return monkeys;
    }

    Day21(Map<String, String> monkeys) {
        this.monkeys = monkeys;
    }

    long getValue(String name) {
        try {
            long value = Long.parseLong(name);
            return value;
        } catch (NumberFormatException e) {}

        if (values.containsKey(name)) {
            return values.get(name);
        }
        String operation = monkeys.get(name);
        // +, -, *, /
        char[] operationTypes = { '+', '-', '*', '/' };
        for (char operationType: operationTypes) {
            if (operation.contains("" + operationType)) {
                String toSplit = " \\" + operationType + " ";
                String[] parts = operation.split(toSplit);
                long lhs = getValue(parts[0]);
                long rhs = getValue(parts[1]);
                return switch (operationType) {
                    case '+' -> lhs + rhs;
                    case '-' -> lhs - rhs;
                    case '*' -> lhs * rhs;
                    case '/' -> lhs / rhs;
                    default -> throw new RuntimeException("cannot reach this point");
                };
            }
        }
        return getValue(operation);
    }

    String getStringValue(String name) {
        if (name.equals("humn")) {
            return name;
        }
        try {
            Long.parseLong(name);
            return name;
        } catch (NumberFormatException e) {}

        String operation = monkeys.get(name);
        // +, -, *, /
        char[] operationTypes = { '+', '-', '*', '/' };
        for (char operationType: operationTypes) {
            if (operation.contains("" + operationType)) {
                String toSplit = " \\" + operationType + " ";
                String[] parts = operation.split(toSplit);
                String lhs = getStringValue(parts[0]);
                String rhs = getStringValue(parts[1]);
                if (lhs.contains("humn") || rhs.contains("humn")) {
                    return "(" + lhs + " " + operationType + " " + rhs + ")";
                }
                long x = Long.parseLong(lhs);
                long y = Long.parseLong(rhs);
                return Long.toString(switch (operationType) {
                    case '+' -> x + y;
                    case '-' -> x - y;
                    case '*' -> x * y;
                    case '/' -> x / y;
                    default -> throw new RuntimeException("cannot reach this point");
                });
            }
        }
        return getStringValue(operation);
    }

    char getOperation(String operation) {
        char[] operationTypes = { '+', '-', '*', '/' };
        for (char type: operationTypes) {
            if (operation.contains("" + type)) {
                return type;
            }
        }
        return 0;
    }

    long partTwo;

    long applyOperation(char type, long x, long y) {
        return switch (type) {
            case '+' -> x + y;
            case '-' -> x - y;
            case '*' -> x * y;
            case '/' -> x / y;
            default -> throw new RuntimeException("cannot reach this point");
        };
    }

    List<Operation> operations = new ArrayList<>();

    String applyReverse(String name) {
        if (name.equals("humn")) {
            return name;
        }
        String operation = monkeys.get(name);
        if (operation == null) {
            return name;
        }
        char type = getOperation(operation);
        if (type == 0) {
            return operation;
        }
        String[] parts = operation.split(" \\" + type + " ");
        String lhs = applyReverse(parts[0]);
        String rhs = applyReverse(parts[1]);
        if (lhs.equals("humn")) {
            this.operations.add(new Operation(type, Long.parseLong(rhs), false));
            return lhs;
        } else if (rhs.equals("humn")) {
            this.operations.add(new Operation(type, Long.parseLong(lhs), true));
            return rhs;
        } else {
            return Long.toString(applyOperation(type, Long.parseLong(lhs), Long.parseLong(rhs)));
        }
    }

    long applyOperations() {
        long result = this.partTwo;
        for (int i = operations.size() - 1; i >= 0; i--) {
            Operation op = operations.get(i);
            switch (op.type) {
                case '+':
                    // humn + a or a + humn
                    result -= op.value;
                    break;
                case '-':
                    if (op.isLhs) {
                        // a - humn
                        result -= op.value;
                        result = -result;
                    } else {
                        // humn - a
                        result += op.value;
                    }
                    break;
                case '*':
                    result /= op.value;
                    break;
                case '/':
                    // a / humn or humn / a
                    if (op.isLhs) {
                        result /= op.value;
                    } else {
                        result *= op.value;
                    }
                    break;
            }
        }
        return result;
    }

    long partTwo() {
        String[] parts = monkeys.get("root").split(" \\+ ");
        String lhs = getStringValue(parts[0]);
        String rhs = getStringValue(parts[1]);
        if (lhs.contains("humn")) {
            this.partTwo = Long.parseLong(rhs);
            applyReverse(parts[0]);
        } else {
            this.partTwo = Long.parseLong(lhs);
            applyReverse(parts[1]);
        }
        return applyOperations();
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/21.txt";
        Map<String, String> monkeys = parse(filename);
        Day21 solver = new Day21(monkeys);
        System.out.println(solver.getValue("root"));
        long p2 = solver.partTwo();
        System.out.println(p2);
    }
}
