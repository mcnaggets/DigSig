package by.kate;

import by.kate.sevice.KeyGenerator;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyGeneratorTest {

    private KeyGenerator generator = new KeyGenerator();

    private int size = 10;

    @Test
    public void shouldGenerateAPublicKey() {
        final FieldMatrix<BigReal> a = generator.generateAPublicKey(size);
        assertEquals(a.getRowDimension(), size);
        assertEquals(a.getColumnDimension(), size);
    }

    @Test
    public void shouldGenerateCPublicKey() {
        final FieldMatrix<BigReal> a = generator.generateCPublicKey(size);
        assertEquals(a.getRowDimension(), 1);
        assertEquals(a.getColumnDimension(), size);
    }

    @Test
    public void shouldGenerateTPrivateKey() {
        final FieldMatrix<BigReal> a = generator.generateTPrivateKey(size);
        assertEquals(a.getRowDimension(), size);
        assertEquals(a.getColumnDimension(), size);
    }


}
