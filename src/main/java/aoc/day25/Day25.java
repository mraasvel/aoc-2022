package aoc.day25;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day25 {
    static long snafuValue(byte b) {
        return switch (b) {
            case '2' -> 2;
            case '1' -> 1;
            case '0' -> 0;
            case '-' -> -1;
            case '=' -> -2;
            default -> throw new RuntimeException("invalid byte");
        };
    }
    static long snafuToLong(String s) {
        byte[] number = s.getBytes();
        long result = 0;
        for (byte digit : number) {
            long value = snafuValue(digit);
            result = (result * 5) + value;
        }
        return result;
    }

    static String longToSnafu(long n) {
        char[] base = { '0', '1', '2', '=', '-' };
        StringBuilder builder = new StringBuilder();
        long carry = 0;
        while (n != 0) {
            int x = (int) (n % 5);
            char ch = base[x];
            builder.append(ch);
            if (ch == '-' || ch == '=') {
                carry = 1;
            } else {
                carry = 0;
            }
            n = n / 5 + carry;
        }
        if (carry > 0) {
            builder.append('1');
        }
        return builder.reverse().toString();
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/25.txt";
        long n = Files.readAllLines(Paths.get(filename)).stream().map(Day25::snafuToLong).reduce(0L, Math::addExact);
        System.out.println(n);
        String s = longToSnafu(n);
        System.out.println(s);
    }
}
