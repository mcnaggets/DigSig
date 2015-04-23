package by.kate.sevice;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.util.BigReal;
import org.apache.commons.math3.util.BigRealField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Random;

public class KeyGenerator {

    private static final int MAX_RANDOM = 10;

    public FieldMatrix<BigReal> generateAPublicKey(int size) {
        Random random = new Random();
        final FieldMatrix<BigReal> a = MatrixUtils.createFieldMatrix(BigRealField.getInstance(), size, size);
        for (int i = 1; i < size; i++) {
            a.setEntry(i - 1, i, BigReal.ONE);
        }
        for (int i = 0; i < size; i++) {
            a.setEntry(size - 1, i, new BigReal(random.nextInt(MAX_RANDOM)));
        }
        return a;
    }

    public FieldMatrix<BigReal> generateCPublicKey(int size) {
        final FieldMatrix<BigReal> c = MatrixUtils.createFieldMatrix(BigRealField.getInstance(), 1, size);
        c.setEntry(0, 0, BigReal.ONE);
        return c;
    }

    public FieldMatrix<BigReal> generateTPrivateKey(int size) {
        Random random = new Random();
        final FieldMatrix<BigReal> t = MatrixUtils.createFieldIdentityMatrix(BigRealField.getInstance(), size);
        for (int k = 0; k < size - 1; k++) {
            for (int i = k + 1; i < size; i++) {
                t.setEntry(k, i, new BigReal(random.nextInt(MAX_RANDOM)));
            }
        }
        return t;
    }

    public FieldMatrix<BigReal> readFromFile(Path path) throws IOException {
        final byte[] bytes = Files.readAllBytes(path);
        final byte[] decode = Base64.getDecoder().decode(bytes);
        final BigReal[][] encodedMatrix = (BigReal[][]) SerializationUtils.deserialize(decode);
        return MatrixUtils.createFieldMatrix(encodedMatrix);
    }

    public void writeToFile(Path path, FieldMatrix<BigReal> data) throws IOException {
        final byte[] serialize = SerializationUtils.serialize(data.getData());
        final byte[] encode = Base64.getEncoder().encode(serialize);
        Files.write(path, encode);
    }
}
