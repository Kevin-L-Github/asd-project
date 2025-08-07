package unibs.asd.playgrounds;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVStatsAnalyzer {

    public static void main(String[] args) {
        String csvFile = "analysis/results.csv";
        List<MatrixStats> statsList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header line
            br.readLine();
            
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 11) {
                    MatrixStats stats = new MatrixStats(
                        values[0],
                        Integer.parseInt(values[1]),
                        Integer.parseInt(values[2]),
                        Integer.parseInt(values[3]),
                        Integer.parseInt(values[4]),
                        Double.parseDouble(values[5]),
                        Double.parseDouble(values[6]),
                        Double.parseDouble(values[7]),
                        Double.parseDouble(values[8]),
                        Double.parseDouble(values[9]),
                        Double.parseDouble(values[10])
                    );
                    statsList.add(stats);
                }
            }
            
            // Calculate and display statistics
            displayStatistics(statsList);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private static void displayStatistics(List<MatrixStats> statsList) {
        if (statsList.isEmpty()) {
            System.out.println("No data to analyze.");
            return;
        }

        System.out.println("=== CSV Statistics Analysis ===");
        System.out.println("Number of matrices: " + statsList.size());
        System.out.println();

        // Calculate averages
        double avgDensity = statsList.stream().mapToDouble(MatrixStats::getDensity).average().orElse(0);
        double avgSparsity = statsList.stream().mapToDouble(MatrixStats::getSparsity).average().orElse(0);
        double avgOnesPerRow = statsList.stream().mapToDouble(MatrixStats::getAvgOnesPerRow).average().orElse(0);
        double avgStdDev = statsList.stream().mapToDouble(MatrixStats::getStdDev).average().orElse(0);

        // Calculate max values
        double maxDensity = statsList.stream().mapToDouble(MatrixStats::getDensity).max().orElse(0);
        double maxOnesPerRow = statsList.stream().mapToDouble(MatrixStats::getMaxOnesPerRow).max().orElse(0);
        double maxStdDev = statsList.stream().mapToDouble(MatrixStats::getStdDev).max().orElse(0);

        // Calculate min values
        double minDensity = statsList.stream().mapToDouble(MatrixStats::getDensity).min().orElse(0);
        double minOnesPerRow = statsList.stream().mapToDouble(MatrixStats::getMinOnesPerRow).min().orElse(0);
        double minStdDev = statsList.stream().mapToDouble(MatrixStats::getStdDev).min().orElse(0);

        System.out.println("=== Density Statistics ===");
        System.out.printf("Average Density: %.4f%n", avgDensity);
        System.out.printf("Max Density: %.4f%n", maxDensity);
        System.out.printf("Min Density: %.4f%n", minDensity);
        System.out.println();

        System.out.println("=== Sparsity Statistics ===");
        System.out.printf("Average Sparsity: %.4f%n", avgSparsity);
        System.out.println();

        System.out.println("=== Ones per Row Statistics ===");
        System.out.printf("Average Ones per Row: %.2f%n", avgOnesPerRow);
        System.out.printf("Max Ones per Row: %.2f%n", maxOnesPerRow);
        System.out.printf("Min Ones per Row: %.2f%n", minOnesPerRow);
        System.out.println();

        System.out.println("=== Standard Deviation Statistics ===");
        System.out.printf("Average StdDev: %.2f%n", avgStdDev);
        System.out.printf("Max StdDev: %.2f%n", maxStdDev);
        System.out.printf("Min StdDev: %.2f%n", minStdDev);
        System.out.println();

        // Find matrix with highest density
        statsList.stream()
            .max((s1, s2) -> Double.compare(s1.getDensity(), s2.getDensity()))
            .ifPresent(stats -> System.out.println("Matrix with highest density: " + stats.getFilename() + " (" + stats.getDensity() + ")"));

        // Find matrix with lowest standard deviation
        statsList.stream()
            .min((s1, s2) -> Double.compare(s1.getStdDev(), s2.getStdDev()))
            .ifPresent(stats -> System.out.println("Matrix with lowest std deviation: " + stats.getFilename() + " (" + stats.getStdDev() + ")"));
    }

    static class MatrixStats {
        private String filename;
        private int rows;
        private int columns;
        private int nonEmptyRows;
        private int nonEmptyCols;
        private double density;
        private double sparsity;
        private double avgOnesPerRow;
        private double maxOnesPerRow;
        private double minOnesPerRow;
        private double stdDev;

        public MatrixStats(String filename, int rows, int columns, int nonEmptyRows, int nonEmptyCols,
                          double density, double sparsity, double avgOnesPerRow, double maxOnesPerRow,
                          double minOnesPerRow, double stdDev) {
            this.filename = filename;
            this.rows = rows;
            this.columns = columns;
            this.nonEmptyRows = nonEmptyRows;
            this.nonEmptyCols = nonEmptyCols;
            this.density = density;
            this.sparsity = sparsity;
            this.avgOnesPerRow = avgOnesPerRow;
            this.maxOnesPerRow = maxOnesPerRow;
            this.minOnesPerRow = minOnesPerRow;
            this.stdDev = stdDev;
        }

        // Getters
        public String getFilename() { return filename; }
        public int getRows() { return rows; }
        public int getColumns() { return columns; }
        public int getNonEmptyRows() { return nonEmptyRows; }
        public int getNonEmptyCols() { return nonEmptyCols; }
        public double getDensity() { return density; }
        public double getSparsity() { return sparsity; }
        public double getAvgOnesPerRow() { return avgOnesPerRow; }
        public double getMaxOnesPerRow() { return maxOnesPerRow; }
        public double getMinOnesPerRow() { return minOnesPerRow; }
        public double getStdDev() { return stdDev; }
    }
}