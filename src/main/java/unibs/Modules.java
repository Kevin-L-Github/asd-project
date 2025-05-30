package unibs;

import java.util.ArrayList;
import java.util.List;

public class Modules {

    public List<int[]> generateChildren(int[] h, List<int[]> current) {
        List<int[]> children = new ArrayList<>();

        boolean empty = true;
        
        for(int i = 0; i < h.length; i++) {
            if (h[i] == 1) {
                empty = false;
                break;
            }
        }
        if (empty) {
            for(int i = 0; i < h.length; i++) {
                int[] child = h;
                child[i] = 1;
                children.add(child);
            }
            return children;
        }

        int[] hp = current.getFirst();

        for (int i = 1; i < LM1(h) - 1; i++){
            int[] h1 = h;
            h1[i] = 1;
            // setFields(h1);
            propagate(h, h1);
            int[] h2i = initial(h, h1);
            int[] h2f = final_(h, h1);

        }
        

        return children;
    }

    private int[] initial(int[] h, int[] h1) {
//        List <int[]> = new ArrayList<>();
        for(int i = 0; i < LM1(h); i++) {
            h[i] = 1;
        }

        return null;
    }

    private int[] final_(int[] h, int[] h1) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'final_'");
    }

    public int LM1(int[] h){
        for(int i = 0; i < h.length; i++) {
            if (h[i] == 1) {
                return i+1;
            }
        }
        return 0;
    }

    public void propagate(int[] h, int[] h1) {
        if(h1.length != h.length) {
            throw new IllegalArgumentException("h1 must have the same length as h");
        }
        // Operate OR Bitwise
        for(int i = 0; i < h.length; i++) {
            h1[i] = h[i] | h1[i];
        }
    }
}
