package unibs.asd.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BenchmarkReader {

    public static boolean[][] readBenchmark(String filePath) {
        System.out.printf("Reading benchmark %s%n", filePath);

        List<String> lines = readAndFilterFile(filePath);
        if (lines.isEmpty()) {
            System.out.println("No valid content found in file.");
            return new boolean[0][0];
        }

        String content = String.join(System.lineSeparator(), lines);
        boolean[][] matrix = parseContentToBooleanMatrix(content);

        return matrix;
    }

    private static List<String> readAndFilterFile(String filePath) {
        Path path = Paths.get(filePath);

        try (Stream<String> lines = Files.lines(path)) {
            return lines.filter(line -> !line.startsWith(";"))
                    .filter(line -> !line.trim().isEmpty())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return List.of();
        }
    }

    private static boolean[][] parseContentToBooleanMatrix(String content) {
        String[] blocks = content.split("-");

        List<boolean[]> rows = new ArrayList<>();

        for (String block : blocks) {
            block = block.trim();
            if (!block.isEmpty()) {
                try {
                    List<Boolean> booleanList = Arrays.stream(block.split("\\s+"))
                            .map(s -> {
                                if (s.equalsIgnoreCase("true") || s.equals("1")) {
                                    return true;
                                } else if (s.equalsIgnoreCase("false") || s.equals("0")) {
                                    return false;
                                } else {
                                    throw new IllegalArgumentException("Invalid boolean value: " + s);
                                }
                            })
                            .collect(Collectors.toList());

                    boolean[] row = new boolean[booleanList.size()];
                    for (int i = 0; i < booleanList.size(); i++) {
                        row[i] = booleanList.get(i);
                    }
                    rows.add(row);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error parsing boolean values in block: " + block);
                    System.err.println(e.getMessage());
                }
            }
        }

        return rows.toArray(new boolean[0][]);
    }

}