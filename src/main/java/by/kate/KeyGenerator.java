package by.kate;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Random;

public class KeyGenerator {

    private static final int MAX_RANDOM = 10;

    public RealMatrix generateAPublicKey(int size) {
        Random random = new Random();
        final RealMatrix a = MatrixUtils.createRealMatrix(size, size);
        for (int i = 1; i < size; i++) {
            a.setEntry(i - 1, i, 1);
        }
        for (int i = 0; i < size; i++) {
            a.setEntry(size - 1, i, random.nextInt(MAX_RANDOM));
        }
        return a;
    }

    public RealMatrix generateCPublicKey(int size) {
        final RealMatrix c = MatrixUtils.createRealMatrix(1, size);
        c.setEntry(0, 0, 1);
        return c;
    }

    public RealMatrix generateTPrivateKey(int size) {
        Random random = new Random();
        final RealMatrix t = MatrixUtils.createRealIdentityMatrix(size);
        for (int k = 0; k < size - 1; k++) {
            for (int i = k + 1; i < size; i++) {
                t.setEntry(k, i, random.nextInt(MAX_RANDOM));
            }
        }
        return t;
    }
}
