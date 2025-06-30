package unibs.asd.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import unibs.asd.benchmarks.BenchmarkReader;
import unibs.asd.benchmarks.BenchmarkWriter;

public class Experiment {

    public static void runBenchmarks(String benchmarkDir, String destDir, int maxFiles) throws IOException {

        try (Stream<Path> filesStream = Files.list(Paths.get(benchmarkDir))) {
            filesStream
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName))
                    .limit(maxFiles)
                    .forEach(path -> processBenchmarkFile(path, destDir));
        }

    }

    private static void processBenchmarkFile(Path filePath, String destDir) {
        String fileName = filePath.getFileName().toString();

        System.out.println("Processing file: " + fileName);

        boolean[][] instance = BenchmarkReader.readBenchmark(filePath.toString());
        MHS mhs = new MHS(instance);
        mhs.run(10_000);

        BenchmarkWriter.writeBenchmark(mhs, fileName, destDir);
    }

}
