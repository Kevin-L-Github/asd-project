package unibs.asd.project;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class App {
    public static void main(String[] args) {
        String benchmarkDir = "src/benchmarks1";
        String outputFile = "benchmark_results.txt";
        int maxFilesToProcess = 1; // Limite di file da processare

        try {
            processBenchmarkDirectory(benchmarkDir, outputFile, maxFilesToProcess);
            System.out.println("Processing completed. Results saved to " + outputFile);
        } catch (IOException e) {
            System.err.println("Error processing benchmark files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processBenchmarkDirectory(String dirPath, String outputPath, int maxFiles) throws IOException {
        try (FileWriter writer = new FileWriter(outputPath)) {
            // Intestazione del file di output
            writer.write("File\tSoluzioni\tTempo(ms)\n");
            writer.write("--------------------------------\n");
            
            // Usa try-with-resources per chiudere automaticamente lo stream
            try (Stream<Path> filesStream = Files.list(Paths.get(dirPath))) {
                filesStream
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::getFileName))
                        .limit(maxFiles) // Prende solo i primi N file
                        .forEach(path -> processBenchmarkFile(path, writer));
            }
        }
    }

    private static void processBenchmarkFile(Path filePath, FileWriter writer) {
        long startTime = System.currentTimeMillis();
        String fileName = filePath.getFileName().toString();
        
        try {
            System.out.println("Processing file: " + fileName);

            boolean[][] instance = BenchmarkReader.readBenchmark(filePath.toString());
            MHS mhs = new MHS(instance);
            mhs.run();

            int solutionCount = mhs.getSolutions().size();
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            String resultLine = String.format("%s\t%d\t%d s\n", fileName, solutionCount, duration/1000);
            writer.write(resultLine);

            System.out.printf("  Found %d solutions in %d s\n", solutionCount, duration/1000);

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            System.err.println("Error processing file " + fileName + ": " + e.getMessage());
            try {
                writer.write(String.format("%s\tERROR\t%dms\n", fileName, duration));
            } catch (IOException ioException) {
                System.err.println("Could not write error to output file: " + ioException.getMessage());
            }
        }
    }
}