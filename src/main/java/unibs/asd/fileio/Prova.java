package unibs.asd.fileio;

import java.io.File;

public class Prova {
    public static void main(String[] args) throws Exception {
        File file = new File("results/30_000ms/74L85.012.mhs");
        AnalysisResult result = OutputAnalyzer.analyze(file);
        System.out.println(result);
    }
}
