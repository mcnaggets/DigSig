package by.kate.sevice;

import by.kate.util.Utils;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.util.BigReal;
import org.apache.commons.math3.util.BigRealField;

public class SignAlgorithm {

    public BigReal[][] encode(byte[] x0, FieldMatrix<BigReal> aMatrix, FieldMatrix<BigReal> tMatrix) {
        final int length = x0.length;
        final FieldMatrix<BigReal> x0Column = MatrixUtils.createColumnFieldMatrix(Utils.toBigRealArray(x0));
        final FieldMatrix<BigReal> tMatrixInverse = getSolver(tMatrix).getInverse();
        FieldMatrix<BigReal> z = MatrixUtils.createFieldMatrix(BigRealField.getInstance(), length, length);
        z.setColumn(0, tMatrixInverse.multiply(x0Column).getColumn(0));
        for (int i = 1; i < length; i++) {
            final FieldMatrix<BigReal> t_1AT = tMatrixInverse.multiply(aMatrix).multiply(tMatrix);
            z.setColumn(i, t_1AT.multiply(z.getColumnMatrix(i - 1)).getColumn(0));
        }
        return z.getData();
    }

    public byte[] decode(BigReal[][] z, FieldMatrix<BigReal> tMatrix, FieldMatrix<BigReal> cMatrix) {
        final int length = z.length;
        final FieldMatrix<BigReal> zMatrix = MatrixUtils.createFieldMatrix(z);
        final byte[] y = new byte[length];
        for (int i = 0; i < length; i++) {
            y[i] = (byte) cMatrix.multiply(tMatrix).multiply(zMatrix.getColumnMatrix(i)).getEntry(0, 0).doubleValue();
        }
        return y;
    }

    private FieldDecompositionSolver<BigReal> getSolver(FieldMatrix<BigReal> tMatrixInverse) {
        return new FieldLUDecomposition<>(tMatrixInverse).getSolver();
    }

}
