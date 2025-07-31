package unibs.asd.fileio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A utility class for reading benchmark files containing boolean matrices.
 * The file format expects:
 * - Lines starting with ';' are treated as comments and ignored
 * - Empty lines are ignored
 * - Matrix rows are separated by '-' characters
 * - Boolean values can be represented as "true"/"false", "1"/"0", or
 * "TRUE"/"FALSE"
 */
public class BenchmarkReader {

    /**
     * Reads a benchmark file and converts it to a boolean matrix.
     * 
     * @param filePath Path to the benchmark file
     * @return A 2D boolean array representing the matrix, or empty array if file is
     *         invalid
     */
    public static boolean[][] readBenchmark(String filePath) {
        //System.out.printf("Reading benchmark %s%n", filePath);

        List<String> lines = readAndFilterFile(filePath);
        if (lines.isEmpty()) {
            System.out.println("No valid content found in file.");
            return new boolean[0][0];
        }

        String content = String.join(System.lineSeparator(), lines);
        return parseContentToBooleanMatrix(content);
    }

    /**
     * Reads a file and filters out comments and empty lines.
     * 
     * @param filePath Path to the file to read
     * @return List of non-comment, non-empty lines
     */
    private static List<String> readAndFilterFile(String filePath) {
        Path path = Paths.get(filePath);

        try (Stream<String> lines = Files.lines(path)) {
            return lines.filter(line -> !line.startsWith(";")) // Remove comment lines
                    .filter(line -> !line.trim().isEmpty()) // Remove empty lines
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Parses the file content into a boolean matrix.
     * 
     * @param content The file content (without comments or empty lines)
     * @return The parsed boolean matrix
     */
    private static boolean[][] parseContentToBooleanMatrix(String content) {
        String[] blocks = content.split("-"); // Split into rows
        List<boolean[]> rows = new ArrayList<>();

        for (String block : blocks) {
            block = block.trim();
            if (!block.isEmpty()) {
                try {
                    boolean[] row = parseBooleanRow(block);
                    rows.add(row);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error parsing boolean values in block: " + block);
                    System.err.println(e.getMessage());
                }
            }
        }

        return rows.toArray(new boolean[0][]);
    }

    /**
     * Parses a single row of boolean values.
     * 
     * @param block String containing space-separated boolean values
     * @return Array of boolean values
     * @throws IllegalArgumentException if invalid boolean values are found
     */
    private static boolean[] parseBooleanRow(String block) {
        List<Boolean> booleanList = Arrays.stream(block.split("\\s+"))
                .map(BenchmarkReader::parseBooleanValue)
                .collect(Collectors.toList());

        boolean[] row = new boolean[booleanList.size()];
        for (int i = 0; i < booleanList.size(); i++) {
            row[i] = booleanList.get(i);
        }
        return row;
    }

    /**
     * Converts a string representation to a boolean value.
     * 
     * @param value String to convert ("true"/"1" or "false"/"0")
     * @return Corresponding boolean value
     * @throws IllegalArgumentException if the string is not a valid boolean
     *                                  representation
     */
    private static boolean parseBooleanValue(String value) {
        if (value.equalsIgnoreCase("true") || value.equals("1")) {
            return true;
        } else if (value.equalsIgnoreCase("false") || value.equals("0")) {
            return false;
        }
        throw new IllegalArgumentException("Invalid boolean value: " + value);
    }
}