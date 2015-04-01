package by.kate;

import by.kate.sevice.SignAlgorithm;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.util.BigReal;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class SignAlgorithmTest {

    final SignAlgorithm algorithm = new SignAlgorithm();

    final FieldMatrix<BigReal> a = MatrixUtils.createFieldMatrix(new BigReal[][]{
            {new BigReal(0), new BigReal(1), new BigReal(0), new BigReal(0)},
            {new BigReal(0), new BigReal(0), new BigReal(1), new BigReal(0)},
            {new BigReal(0), new BigReal(0), new BigReal(0), new BigReal(1)},
            {new BigReal(3), new BigReal(3), new BigReal(2), new BigReal(6)}
    });

    final FieldMatrix<BigReal> t = MatrixUtils.createFieldMatrix(new BigReal[][]{
            {new BigReal(1), new BigReal(4), new BigReal(11), new BigReal(1)},
            {new BigReal(0), new BigReal(1), new BigReal(6), new BigReal(2)},
            {new BigReal(0), new BigReal(0), new BigReal(1), new BigReal(11)},
            {new BigReal(0), new BigReal(0), new BigReal(0), new BigReal(1)}
    });

    final FieldMatrix<BigReal> c = MatrixUtils.createRowFieldMatrix(new BigReal[]{BigReal.ONE, BigReal.ZERO, BigReal.ZERO, BigReal.ZERO});

    final byte[] x0 = new byte[]{3, 4, 2, 1};

    @Test
    public void shouldEncodeAndDecodeData() {
        final BigReal[][] encode = algorithm.encode(x0, a, t);
        final byte[] decode = algorithm.decode(encode, t, c);
        assertArrayEquals(decode, x0);
    }

}
