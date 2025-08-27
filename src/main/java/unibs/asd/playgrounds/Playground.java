package unibs.asd.playgrounds;

import java.io.IOException;
import java.util.Scanner;

import unibs.asd.enums.BitSetType;
import unibs.asd.fileio.BenchmarkReader;
import unibs.asd.fileio.BenchmarkWriter;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BoostMHS;

public class Playground {

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Inserisci il nome del file benchmark (es. 74L85.000.matrix): ");
        String benchmark = scanner.nextLine();
        
        System.out.print("Inserisci il tempo massimo di esecuzione (in secondi): ");
        long timeout = scanner.nextLong();

        boolean[][] matrix = BenchmarkReader.readBenchmark("src\\mybenchmarks\\" + benchmark);
        MHS mhs = new BoostMHS(matrix);
        mhs.run(BitSetType.FAST_BITSET, timeout * 1000);

        System.out.println("Soluzioni trovate: " + mhs.getSolutions().size());
        System.out.println("Tempo di esecuzione: " + mhs.getComputationTime()/1_000_000_000F + " secondi");
        
        BenchmarkWriter.writeBenchmark(mhs, benchmark, "results");
        scanner.close();
    }

}
