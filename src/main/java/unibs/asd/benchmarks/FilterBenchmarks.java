package unibs.asd.benchmarks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.stream.Stream;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;


public class FilterBenchmarks {

    private static String SOLVED_FILENAME = "solved_benchmarks_e.txt";
    private static int TIMEOUT_MS = 60000; // 2.5 secondi per benchmark

    public static void runBenchmarks(String benchmarkDir, String destDir) throws IOException {
        Path solvedFile = prepareSolvedFile(destDir);
        processBenchmarksSequentially(benchmarkDir, solvedFile);
    }

    private static Path prepareSolvedFile(String destDir) throws IOException {
        Path dirPath = Paths.get(destDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path solvedFile = dirPath.resolve(SOLVED_FILENAME);
        Files.deleteIfExists(solvedFile);
        return Files.createFile(solvedFile);
    }

    private static void processBenchmarksSequentially(String benchmarkDir, Path solvedFile) throws IOException {
        try (Stream<Path> filesStream = Files.list(Paths.get(benchmarkDir))) {
            filesStream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .forEach(path -> processSingleBenchmark(path, solvedFile));
        }
    }

    private static void processSingleBenchmark(Path inputFile, Path solvedFile) {
        String filename = inputFile.getFileName().toString();
        System.out.println("[Processing] " + filename);

        // Lettura input e setup MHS
        boolean[][] instance = BenchmarkReader.readBenchmark(inputFile.toString());
        MHS fastMHS = new BoostMHS(instance);

        long startTime = System.currentTimeMillis();
        fastMHS.run(BitSetType.BITSET,TIMEOUT_MS);

        if(fastMHS.isExecuted() && !fastMHS.isStopped()) {
            writeSolvedBenchmark(filename, solvedFile);
            System.out.println("[Solved] " + filename + " in " + (System.currentTimeMillis() - startTime) + "ms");
        } else {
            System.out.println("[Timeout] " + filename);
        }
    }

    private static void writeSolvedBenchmark(String filename, Path solvedFile) {
        try {
            Files.writeString(solvedFile, filename + "\n", StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[Error] Failed to write " + filename);
        }
    }
}