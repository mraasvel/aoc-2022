package aoc.day22;

import aoc.Point;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Grid {
    enum Square {
        Empty,
        Wall,
    }
    private final HashMap<Point, Square> squares;
    private Point topLeft;
    private final List<Point> sides;

//    private HashMap<Integer, Integer> rowLengths; // row index -> length of row
//    private HashMap<Integer, Integer> colLengths; // column index -> length of column

    static public final boolean example = false;
    static private int SIDE_LENGTH = 50;
//    private final int SIDE_LENGTH = 4;

    boolean isSideTopLeft(Point point) {
        return point.x % SIDE_LENGTH == 0 && point.y % SIDE_LENGTH == 0;
    }

    Grid(String mapData) {
        if (example) {
            SIDE_LENGTH = 4;
        }
        this.squares = new HashMap<>();
        this.sides = new ArrayList<>();
        Point position = new Point(0, 0);
        this.topLeft = null;
        for (String row : mapData.split("\n")) {
            position.x = 0;
            for (int i = 0; i < row.length(); i++) {
                char ch = row.charAt(i);
                if (ch != ' ' && isSideTopLeft(position)) {
                    sides.add(new Point(position));
                }
                if (ch == '.') {
                    if (this.topLeft == null) {
                        this.topLeft = new Point(position);
                    }
                    squares.put(new Point(position), Square.Empty);
                } else if (ch == '#') {
                    squares.put(new Point(position), Square.Wall);
                }
                position.x++;
            }
            position.y++;
        }
        System.out.println(sides);
    }

    // returns true if position or delta changed
    public boolean getNextPosition(Point point, Point delta) {
        if (!squares.containsKey(point)) {
            throw new RuntimeException("original point is out of bounds");
        }
        Point newPoint = point.add(delta);
        if (squares.containsKey(newPoint)) {
            if (getSquare(newPoint) == Square.Wall) {
                return false;
            }
            point.x = newPoint.x;
            point.y = newPoint.y;
            return true;
        }
        if (example) {
            return nextPointOnSideExample(point, delta);
        } else {
            return nextPointOnSide(point, delta);
        }
        // part one
//        // go the other way until the edge
//        Point newDelta = new Point(-delta.x, -delta.y);
//        Point previous = point;
//        Point current = point.add(newDelta);
//        while (squares.containsKey(current)) {
//            previous = current;
//            current = current.add(newDelta);
//        }
//        if (getSquare(previous) == Square.Wall) {
//            return point;
//        }
//        return previous;
    }

    public Square getSquare(Point point) {
        return squares.get(point);
    }

    int getSideIndex(Point point) {
        for (int i = 0; i < sides.size(); i++) {
            Point topLeft = sides.get(i);
            Point bottomRight = topLeft.add(new Point(SIDE_LENGTH, SIDE_LENGTH));
            if (point.x >= topLeft.x && point.x < bottomRight.x && point.y >= topLeft.y && point.y < bottomRight.y) {
                return i;
            }
        }
        throw new RuntimeException("point not in any side");
    }

    // right -> top = single clockwise rotation
    //

    // local point rotations
    Point rotateClockwise(Point point) {
        return new Point(SIDE_LENGTH - point.y - 1, point.x);
    }

    Point rotateCounterClockwise(Point point) {
        return new Point(point.y, SIDE_LENGTH - point.x - 1);
    }

    // wrap local point around
    // i.e if we go right we go back to 0 in X
    Point wrapAround(Point point, Point delta) {
        // R, L, D, U
        if (delta.x == 1) {
            return new Point(0, point.y);
        } else if (delta.x == -1) {
            return new Point(SIDE_LENGTH - 1, point.y);
        } else if (delta.y == 1) {
            return new Point(point.x, 0);
        } else if (delta.y == -1) {
            return new Point(point.x, SIDE_LENGTH - 1);
        }
        throw new RuntimeException("invalid delta");
    }

    static private final Point RIGHT = new Point(1, 0);
    static private final Point DOWN = new Point(0, 1);
    static private final Point LEFT = new Point(-1, 0);
    static private final Point UP = new Point(0, -1);

    // modify point and delta if next point is not a wall
    boolean nextPointOnSide(Point point, Point delta) {
        int side = getSideIndex(point) + 1;
        Point local = localPoint(point, sides.get(side - 1));
        int toSide;

        Point newDelta;
        Point newPoint;

        if (side == 1 && delta.equals(LEFT)) {
            // 1 L -> L 4
            newDelta = RIGHT;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 4;
        } else if (side == 1 && delta.equals(UP)) {
            // 1 T -> L 6
            // RC(1)
            newDelta = RIGHT;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 6;
        } else if (side == 2 && delta.equals(UP)) {
            // 2 T -> B 6
            newDelta = UP;
            newPoint = wrapAround(local, newDelta);
            toSide = 6;
        } else if (side == 2 && delta.equals(RIGHT)) {
            // 2 R -> R 5
            newDelta = LEFT;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 5;
        } else if (side == 2 && delta.equals(DOWN)) {
            // 2 B -> R 3
            newDelta = LEFT;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 3;
        } else if (side == 3 && delta.equals(LEFT)) {
            // 3 L -> T 4
            newDelta = DOWN;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 4;
        } else if (side == 3 && delta.equals(RIGHT)) {
            // 3 R -> B 2
            newDelta = UP;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 2;
        } else if (side == 4 && delta.equals(UP)) {
            // 4 T -> L 3
            newDelta = RIGHT;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 3;
        } else if (side == 4 && delta.equals(LEFT)) {
            // 4 L -> L 1
            newDelta = RIGHT;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 1;
        } else if (side == 5 && delta.equals(RIGHT)) {
            // 5 R -> R 2
            newDelta = LEFT;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 2;
        } else if (side == 5 && delta.equals(DOWN)) {
            // 5 D -> R 6
            newDelta = LEFT;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 6;
        } else if (side == 6 && delta.equals(LEFT)) {
            // 6 L -> T 1
            newDelta = DOWN;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 1;
        } else if (side == 6 && delta.equals(DOWN)) {
            // 6 D -> T 2
            newDelta = DOWN;
            newPoint = wrapAround(local, newDelta);
            toSide = 2;
        } else if (side == 6 && delta.equals(RIGHT)) {
            // 6 R -> D 5
            newDelta = UP;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 5;
        } else {
            throw new RuntimeException("shouldn't rotate from this position with this delta");
        }
        newPoint = newPoint.add(sides.get(toSide - 1));
        if (getSquare(newPoint) == Square.Wall) {
            return false;
        }
        point.x = newPoint.x;
        point.y = newPoint.y;
        delta.x = newDelta.x;
        delta.y = newDelta.y;
        return true;
    }

    boolean nextPointOnSideExample(Point point, Point delta) {
        int side = getSideIndex(point) + 1;
        Point local = localPoint(point, sides.get(side - 1));
        int toSide;

        Point newDelta;
        Point newPoint;
        if (side == 1 && delta.equals(LEFT)) {
            // L1 -> T3
            newDelta = DOWN;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 3;
        } else if (side == 1 && delta.equals(UP)) {
            // T1 -> T2
            newDelta = DOWN;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 2;
        } else if (side == 1 && delta.equals(RIGHT)) {
            // R1 -> R6
            newDelta = LEFT;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 6;
        } else if (side == 2 && delta.equals(DOWN)) {
            // B2 -> B5
            newDelta = UP;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 5;
        } else if (side == 2 && delta.equals(LEFT)) {
            // L2 -> B6
            newDelta = UP;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 6;
        } else if (side == 2 && delta.equals(UP)) {
            // T2 -> T1
            newDelta = DOWN;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 1;
        } else if (side == 3 && delta.equals(DOWN)) {
            // D3 -> L5
            newDelta = RIGHT;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 5;
        } else if (side == 3 && delta.equals(UP)) {
            // T3 -> L1
            newDelta = RIGHT;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 1;
        } else if (side == 4 && delta.equals(RIGHT)) {
            // R4 -> T6
            newDelta = DOWN;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 6;
        } else if (side == 5 && delta.equals(DOWN)) {
            // D5 -> D2
            newDelta = UP;
            newPoint = wrapAround(rotateClockwise(rotateClockwise(local)), newDelta);
            toSide = 2;
        } else if (side == 5 && delta.equals(LEFT)) {
            // L5 -> D3
            newDelta = UP;
            newPoint = wrapAround(rotateClockwise(local), newDelta);
            toSide = 3;
        } else if (side == 6 && delta.equals(DOWN)) {
            // D6 -> L2
            newDelta = RIGHT;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 2;
        } else if (side == 6 && delta.equals(UP)) {
            // U6 -> R4
            newDelta = LEFT;
            newPoint = wrapAround(rotateCounterClockwise(local), newDelta);
            toSide = 4;
        } else if (side == 6 && delta.equals(RIGHT)) {
            // R6 -> R1
            newDelta = LEFT;
            newPoint = wrapAround((rotateClockwise(rotateClockwise(local))), newDelta);
            toSide = 1;
        } else {
            throw new RuntimeException("shouldn't rotate from this position with this delta");
        }
        newPoint = newPoint.add(sides.get(toSide - 1));
        if (getSquare(newPoint) == Square.Wall) {
            return false;
        }
        point.x = newPoint.x;
        point.y = newPoint.y;
        delta.x = newDelta.x;
        delta.y = newDelta.y;
        return true;
    }


    Point localPoint(Point point, Point sideTopLeft) {
        return new Point(point.x - sideTopLeft.x, point.y - sideTopLeft.y);
    }

    public Point getTopLeft() {
        return new Point(topLeft);
    }
}

public class Day22 {
    Grid grid;
    String instructions;

    Day22(String filename) throws IOException {
        String[] content = Files.readString(Paths.get(filename)).split("\n\n");
        this.grid = new Grid(content[0]);
        this.instructions = content[1];
    }

    int getFacing(Point delta) {
        // R D L U
        List<Point> directions = new ArrayList<>(){{
            add(new Point(1, 0));
            add(new Point(0, 1));
            add(new Point(-1, 0));
            add(new Point(0, -1));
        }};
        for (int i = 0; i < directions.size(); i++) {
            if (delta.equals(directions.get(i))) {
                return i;
            }
        }
        throw new RuntimeException("invalid delta");
    }

    int partOne() {
        Matcher matcher = Pattern.compile("((\\d+)|(L)|(R))").matcher(instructions);
        Point current = grid.getTopLeft();
        Point delta = new Point(1, 0);
        while (matcher.find()) {
            String instruction = matcher.group(1);
            if (instruction.equals("L")) {
                delta = new Point(delta.y, -delta.x);
            } else if (instruction.equals("R")) {
                delta = new Point(-delta.y, delta.x);
            } else {
                int n = Integer.parseInt(instruction);
                while (n > 0) {
                    if (!grid.getNextPosition(current, delta)) {
                        break;
                    }
                    n--;
                }
            }
        }
        int row = current.y + 1;
        int col = current.x + 1;
        int facing = getFacing(delta);
        System.out.printf("%d %d %d\n", row, col, facing);
        return row * 1000 + col * 4  + facing;
    }
    public static void main(String[] args) throws IOException {
        String filename = "inputs/22.txt";
        if (Grid.example) {
            filename = "inputs/ex.txt";
        }
        Day22 solver = new Day22(filename);
        int p1 = solver.partOne();
        System.out.println(p1);
    }
}
