package aoc;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

enum PacketType {
    List,
    Integer
}

interface Packet {
    // returns -1 on A is smaller, 0 on equal, 1 on other is smaller
    int compareTo(Packet rhs);
    PacketType getType();
}

class Parser {
    int index;
    String s;

    Parser(String s) {
        this.s = s;
        this.index = 0;
    }

    Packet parse() {
        if (peek() == '[') {
            return parsePacketList();
        } else {
            return parsePacketInteger();
        }
    }

    PacketList parsePacketList() {
        index++;
        List<Packet> packets = new ArrayList<>();
        while (index < s.length()) {
            if (peek() == ']') {
                index++;
                break;
            } else if (peek() == ',') {
                index++;
            } else {
                Packet packet = parse();
                packets.add(packet);
            }
        }
        return new PacketList(packets);
    }

    PacketInt parsePacketInteger() {
        // have to find end of integer
        int end = index;
        do {
            char ch = s.charAt(end);
            if (ch == ',' || ch == ']')  break;
            end++;
        } while (end < s.length());
        PacketInt packet = new PacketInt(Integer.parseInt(s, index, end, 10));
        index = end;
        return packet;
    }

    private char peek() {
        return s.charAt(index);
    }
}

class PacketList implements Packet {
    List<Packet> packets;

    PacketList(List<Packet> packets) {
        this.packets = packets;
    }

    public int compareTo(Packet rhs) {
        if (rhs.getType() == PacketType.Integer) {
            return this.compareTo(((PacketInt)rhs).toPacketList());
        }
        PacketList other = (PacketList)rhs;
        for (int i = 0; i < packets.size(); i++) {
            if (i >= other.packets.size()) {
                return 1;
            }
            Packet a = packets.get(i);
            Packet b = other.packets.get(i);
            int result = a.compareTo(b);
            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            }
        }
        if (packets.size() < other.packets.size()) {
            return -1;
        } else {
            return 0;
        }
    }

    public PacketType getType() {
        return PacketType.List;
    }

    @Override
    public String toString() {
        return packets.toString();
    }
}

class PacketInt implements Packet {
    private final int n;

    PacketInt(int n) {
        this.n = n;
    }

    public int compareTo(Packet rhs) {
        if (rhs.getType() == PacketType.List) {
            return this.toPacketList().compareTo(rhs);
        }
        PacketInt other = (PacketInt)rhs;
        return Integer.compare(n, other.n);
    }

    public PacketType getType() {
        return PacketType.Integer;
    }

    public PacketList toPacketList() {
        return new PacketList(new ArrayList<>(){{
            add(new PacketInt(n));
        }});
    }

    @Override
    public String toString() {
        return Integer.toString(n);
    }
}

public class Day13 {
    // nested list is always of size 2
    List<List<Packet>> packets;
    Day13(String filename) throws IOException {
        String[] pairs = Files.readString(Paths.get(filename)).split("\n\n");
        this.packets = Arrays.stream(pairs).map(p ->
            Arrays.stream(p.split("\n"))
                    .map(packet -> new Parser(packet).parse())
                    .collect(Collectors.toCollection(ArrayList::new))
        ).collect(Collectors.toCollection(ArrayList::new));
    }

    public static void main(String[] args) throws IOException {
        String filename = "inputs/13.txt";
        Day13 solver = new Day13(filename);
        int p1 = solver.partOne();
        System.out.println(p1);
        int p2 = solver.partTwo();
        System.out.println(p2);
    }

    int partOne() {
        int sum = 0;
        for (int i = 0; i < packets.size(); i++) {
            List<Packet> pair = packets.get(i);
            int result = pair.get(0).compareTo(pair.get(1));
            if (result < 0) {
                sum += i + 1;
            }
        }
        return sum;
    }

    int findSortedPosition(Packet x) {
        int position = 0;
        for (List<Packet> pair : packets) {
            for (Packet packet : pair) {
                if (x.compareTo(packet) > 0) {
                    position++;
                }
            }
        }
        return position;
    }

    // we only have to find the sorted position of a and b
    int partTwo() {
        Packet a = new Parser("[[2]]").parse();
        Packet b = new Parser("[[6]]").parse();
        // index starts at 1, b is larger than a so we add 2 there
        int sortedA = findSortedPosition(a) + 1;
        int sortedB = findSortedPosition(b) + 2;
        return sortedA * sortedB;
    }
}
