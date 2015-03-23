package by.kate;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyGeneratorTest {

    private KeyGenerator generator = new KeyGenerator();

    private int size = 10;

    @Test
    public void shouldGenerateAPublicKey() {
        final RealMatrix a = generator.generateAPublicKey(size);
        assertEquals(a.getRowDimension(), size);
        assertEquals(a.getColumnDimension(), size);
    }

    @Test
    public void shouldGenerateCPublicKey() {
        final RealMatrix a = generator.generateCPublicKey(size);
        assertEquals(a.getRowDimension(), 1);
        assertEquals(a.getColumnDimension(), size);
    }

    @Test
    public void shouldGenerateTPrivateKey() {
        final RealMatrix a = generator.generateTPrivateKey(size);
        assertEquals(a.getRowDimension(), size);
        assertEquals(a.getColumnDimension(), size);
    }


}
