package by.kate;

import org.apache.commons.math3.linear.*;

public class SignAlgorithm {

    public double[][] encode(byte[] x0, RealMatrix aMatrix, RealMatrix tMatrix) {
        final int length = x0.length;
        final RealMatrix x0Column = MatrixUtils.createColumnRealMatrix(Utils.toDoubleArray(x0));
        final RealMatrix tMatrixInverse = getSolver(tMatrix).getInverse();
        RealMatrix z = MatrixUtils.createRealMatrix(length, length);
        z.setColumn(0, tMatrixInverse.multiply(x0Column).getColumn(0));
        for (int i = 1; i < length; i++) {
            final RealMatrix t_1AT = tMatrixInverse.multiply(aMatrix).multiply(tMatrix);
            z.setColumn(i, t_1AT.multiply(z.getColumnMatrix(i - 1)).getColumn(0));
        }
        return z.getData();
    }

    public byte[] decode(double[][] z, RealMatrix tMatrix, RealMatrix cMatrix) {
        final int length = z.length;
        final RealMatrix zMatrix = MatrixUtils.createRealMatrix(z);
        final byte[] y = new byte[length];
        for (int i = 0; i < length; i++) {
            y[i] = (byte) cMatrix.multiply(tMatrix).multiply(zMatrix.getColumnMatrix(i)).getEntry(0, 0);
        }
        return y;
    }

    private DecompositionSolver getSolver(RealMatrix tMatrixInverse) {
        return new LUDecomposition(tMatrixInverse).getSolver();
    }

}
