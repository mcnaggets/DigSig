package by.kate;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SignAlgorithmTest {

    final SignAlgorithm algorithm = new SignAlgorithm();

    final RealMatrix a = MatrixUtils.createRealMatrix(new double[][]{
            {0, 1, 0, 0},
            {0, 0, 1, 0},
            {0, 0, 0, 1},
            {3, 3, 2, 6}
    });

    final RealMatrix t = MatrixUtils.createRealMatrix(new double[][]{
            {1, 4, 11, 1},
            {0, -1, 6, 2},
            {0, 0, 1, 11},
            {0, 0, 0, -1}
    });

    final RealMatrix c = MatrixUtils.createRowRealMatrix(new double[]{1, 0, 0, 0});

    final byte[] x0 = new byte[]{3, 4, 2, 1};

    @Test
    public void shouldEncodeAndDecodeData() {
        final double[][] encode = algorithm.encode(x0, a, t);
        final byte[] decode = algorithm.decode(encode, t, c);
        assertArrayEquals(decode, x0);
    }

}
