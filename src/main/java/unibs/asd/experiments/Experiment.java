package unibs.asd.experiments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

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
        MHS mhs = new BoostMHS(instance);
        mhs.run(BitSetType.BITSET,10_000);
    }

}
