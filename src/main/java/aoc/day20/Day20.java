package aoc.day20;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class NumberWrapper {
    long value;

    NumberWrapper(String s) {
        this.value = Long.parseLong(s);
    }
    NumberWrapper(long n) {
        this.value = n;
    }

    long intValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }
}

public class Day20 {

    public static void main(String[] args) throws IOException {
        String filename = "inputs/20.txt";


        List<NumberWrapper> numbers;
        try (Stream<String> lines = Files.lines(Paths.get(filename))) {
            final long KEY = 811589153;
            numbers = lines.map(NumberWrapper::new).map(n -> new NumberWrapper(n.value * KEY)).collect(Collectors.toCollection(ArrayList::new));
        }
        List<NumberWrapper> order = new ArrayList<>(numbers);
        for (int i = 0; i < 10; i++) {
            for (NumberWrapper toMove : order) {
                int index = numbers.indexOf(toMove);
                toMove = numbers.remove(index);
                long newIndex = toMove.intValue() + index;
                if (newIndex < 0) {
                    newIndex = numbers.size() - (-newIndex % numbers.size());
                } else {
                    newIndex = newIndex % numbers.size();
                }
                if (newIndex == 0) {
                    numbers.add(toMove);
                } else {
                    numbers.add((int)newIndex, toMove);
                }
            }
        }
        int zero = numbers.indexOf(numbers.stream().filter(x -> x.value == 0).findFirst().get());
        long a = numbers.get((zero + 1000) % numbers.size()).intValue();
        long b = numbers.get((zero + 2000) % numbers.size()).intValue();
        long c = numbers.get((zero + 3000) % numbers.size()).intValue();
        System.out.printf("%d,%d,%d\n", a, b, c);
        System.out.println(a + b + c);
    }
}
