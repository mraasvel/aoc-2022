package aoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Day02 {

    int p1 = 0;
    int p2 = 0;

    public void parse(String filename) {
        p1 = 0;
        p2 = 0;
        try {
            Scanner scanner = new Scanner(new File((filename)));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                do_move(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    // 0 beats 2
    // 1 beats 0
    // 2 beats 1
    // N = N+1 point

    private int score_p1(char a, char b) {
        a -= 'A';
        b -= 'X';
        int score = b + 1;
        if (a == b) {
            score += 3;
        } else if ((b + 2) % 3 == a) {
            score += 6;
        }
        return score;
    }

    private int score_p2(char a, char b) {
        a -= 'A';
        b -= 'X';
        int score = 0;
        if (b == 0) {
            // lose
            score += ((a + 2) % 3) + 1;
        } else if (b == 1) {
            // draw
            score += 3;
            score += a + 1;
        } else {
            // win
            score += 6;
            score += ((a + 1) % 3) + 1;
        }
        return score;
    }

    private void do_move(String move) {
        // 3 for draw, 6 for win
        // A,B,C = R,P,S
        // X,Y,Z = R,P,S
        char x = move.charAt(0);
        char y = move.charAt(2);
        p1 += score_p1(x, y);
        p2 += score_p2(x, y);
    }

    public static void main(String[] args) {
        Day02 day02 = new Day02();
        day02.parse("inputs/02.txt");
        System.out.printf("P1: %d\n", day02.p1);
        System.out.printf("P2: %d\n", day02.p2);
    }
}
