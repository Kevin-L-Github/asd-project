package unibs.asd.playgrounds;

import unibs.asd.enums.BitSetType;
import unibs.asd.interfaces.MHS;
import unibs.asd.mhs.BaseMHS;
import unibs.asd.mhs.BoostMHS;

public class MyScript {

    public static void main(String[] args) {

        int max = 24;
        long maxTime = 3600 * 8;

        for (int i = 26; i <= max; i = i + 2) {
            boolean[][] matrix = IdentityMatrix.create(i);
            MHS mhs = new BoostMHS(matrix);
            mhs.run(BitSetType.FAST_BITSET, maxTime * 1000);
            System.out.println(i + "\t" + mhs.getSolutions().size() + "\t" + mhs.getComputationTime() / 1_000_000_000F
                    + " secondi");
            mhs = null;
            System.gc();
        }

    }

}
