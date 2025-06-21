package unibs.asd.project;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

public class Experiment {
    private final String benchmarkDir;
    private final String outputFile;
    private final int maxFiles;

    public Experiment(String benchmarkDir, String outputFile, int maxFiles) {
        this.benchmarkDir = benchmarkDir;
        this.outputFile = outputFile;
        this.maxFiles = maxFiles;
    }

    // Metodo principale che esegue tutto l'esperimento
    public static void runBenchmarks(String benchmarkDir, String outputFile, int maxFiles) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            // Intestazione del file di output
            writer.write("File\tSoluzioni\tTempo(ms)\n");
            writer.write("--------------------------------\n");
            
            // Usa try-with-resources per chiudere automaticamente lo stream
            try (Stream<Path> filesStream = Files.list(Paths.get(benchmarkDir))) {
                filesStream
                        .filter(Files::isRegularFile)
                        .sorted(Comparator.comparing(Path::getFileName))
                        .limit(maxFiles) // Prende solo i primi N file
                        .forEach(path -> processBenchmarkFile(path, writer));
            }
        }
    }

    // Metodo privato che elabora un singolo file di benchmark
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
