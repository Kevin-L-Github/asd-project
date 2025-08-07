package unibs.asd.fileio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class OutputAnalyzer {


    /**
     * Analizza un file di output .mhs
     * 
     * @param file File da analizzare
     * @return AnalysisResult con le informazioni estratte
     * @throws IOException In caso di errore di lettura
     */
    public static AnalysisResult analyze(File file) throws IOException {
        int numSolutions = 0;
        String executionTime = "N/A";
        String status = "Unknown";
        String stopReason = null;

        boolean inSolutionsSection = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Conta le soluzioni
                if (line.equals("=== SOLUTIONS ===")) {
                    inSolutionsSection = true;
                    continue;
                }
                if (line.equals("=================")) {
                    inSolutionsSection = false;
                    continue;
                }
                if (inSolutionsSection && !line.isEmpty() && !line.startsWith("No solutions")) {
                    numSolutions++;
                }

                // Parsing dei commenti
                if (line.startsWith(";;;")) {
                    if (line.contains("Computation time:")) {
                        executionTime = line.replace(";;; Computation time:", "").trim();
                    } else if (line.contains("Execution status:")) {
                        status = line.replace(";;; Execution status:", "").trim();
                    } else if (line.contains("Stopped reason:")) {
                        stopReason = line.replace(";;; Stopped reason:", "").trim();
                    }
                }
            }
        }

        return new AnalysisResult(file.getName(), numSolutions, executionTime, status, stopReason);
    }
}
