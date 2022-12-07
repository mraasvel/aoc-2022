package aoc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

interface CustomFile {
    int getSize();
    boolean isDirectory();
    void print(int indentation);
    String getId();
}

class RegularFile implements CustomFile {
    int size;
    String filename;

    RegularFile(String filename, int size) {
        this.size = size;
        this.filename = filename;
    }

    public int getSize() {
        return size;
    }

    public boolean isDirectory() {
        return false;
    }

    public void print(int indentation) {
        for (int i = 0; i < indentation; i++) {
            System.out.print(" ");
        }
        System.out.printf("- %s (file, size=%d)\n", filename, getSize());
    }

    public String getId() {
        return filename;
    }
}

class Directory implements CustomFile {
    ArrayList<CustomFile> files = new ArrayList<>();
    Directory parent;
    String id;

    Directory(String id, Directory parent) {
        this.parent = parent;
        this.id = id;
    }
    public int getSize() {
        int size = 0;
        for (CustomFile file : files) {
            size += (file.getSize());
        }
        return size;
    }

    public boolean isDirectory() {
        return true;
    }

    void addCustomFile(CustomFile file) {
        files.add(file);
    }

    Directory getParent() {
        return parent;
    }

    public String getId() {
        return id;
    }

    ArrayList<Directory> getDirectories() {
        ArrayList<Directory> dirs = new ArrayList<>();
        for (CustomFile file: files) {
            if (file.isDirectory()) {
                dirs.add((Directory)file);
            }
        }
        return dirs;
    }

    public CustomFile getFileByName(String name) {
        for (CustomFile file: files) {
            if (file.getId().equals(name)) {
                return file;
            }
        }
        return null;
    }

    public void print(int indentation) {
        for (int i = 0; i < indentation; i++) {
            System.out.print(" ");
        }
        System.out.printf(" - %s (dir)\n", getId());
        for (CustomFile file : files) {
            file.print(indentation + 2);
        }
    }
}

public class Day07 {
    Directory root = null;
    Directory current = null;

    final int TOTAL_SIZE = 70000000;
    final int REQUIRED_SIZE = 30000000;

    void processDirectory(Scanner scanner, String id) {
        if (current == null) {
            current = new Directory(id, null);
            root = current;
        } else {
            current = (Directory)current.getFileByName(id);
        }
        if (!scanner.nextLine().equals("$ ls")) {
            throw new RuntimeException("expected ls");
        }
        while (scanner.hasNextLine()) {
            String p = "\\$ cd .*";
            Pattern pattern = Pattern.compile(p);
            if (scanner.hasNext(pattern)) {
                break;
            }
            String line = scanner.nextLine();
            if (line.startsWith("dir")) {
                String subdirId = line.split(" ")[1];
                Directory subdir = new Directory(subdirId, current);
                current.addCustomFile(subdir);
            } else {
                String[] parts = line.split(" ");
                int size = Integer.parseInt(parts[0]);
                String filename = parts[1];
                current.addCustomFile(new RegularFile(filename, size));
            }
        }
    }

    int calculatePartOne(Directory dir) {
        int sum = 0;
        int size = dir.getSize();
        if (size <= 100000) {
            sum += size;
        }
        for (Directory subdir : dir.getDirectories()) {
            sum += (calculatePartOne(subdir));
        }
        return sum;
    }
    void partOne(String filename) {
        try {
            Scanner scanner = new Scanner(new File(filename));
            scanner.useDelimiter("\n");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("$ cd")) {
                    String id = line.split(" ")[2];
                    if (id.equals("..")) {
                        current = current.getParent();
                        continue;
                    }
                    processDirectory(scanner, id);
                } else {
                    throw new RuntimeException(line);
                }
            }
            int size = calculatePartOne(root);
            System.out.printf("p1: %d\n", size);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    int smallest = Integer.MAX_VALUE;

    void calculatePartTwo(Directory dir, int required) {
        int size = dir.getSize();
        if (size >= required && size < smallest) {
            smallest = size;
        }
        for (Directory subdir : dir.getDirectories()) {
            calculatePartTwo(subdir, required);
        }
    }


    void partTwo() {
        int available = TOTAL_SIZE - root.getSize();
        int required = REQUIRED_SIZE - available;
        calculatePartTwo(root, required);
        System.out.printf("available: %d, required: %d\n", available,required);
        System.out.printf("p2: %d\n", smallest);
    }

    public static void main(String[] args) {
        Day07 day07 = new Day07();
        String filename = "inputs/07.txt";
        day07.partOne(filename);
        day07.partTwo();
    }
}
