package by.kate;

import by.kate.sevice.FileSigner;
import by.kate.sevice.KeyGenerator;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class FileSignerTest {

    private KeyGenerator generator = new KeyGenerator();

    private Path tempFile;
    private FileSigner signer;

    @Before
    public void initialize() throws IOException {
        tempFile = Files.createTempFile("text_", ".txt");
        Files.write(tempFile, "temporary file content".getBytes());
        signer = FileSigner.getSigner(tempFile);
    }

    @Test
    public void shouldSignFile() throws IOException {
        final String signature = "Some signatory sample ^)";
        final int size = signature.getBytes().length;
        final FieldMatrix<BigReal> privateKey = generator.generateTPrivateKey(size);
        final FieldMatrix<BigReal> aPublicKey = generator.generateAPublicKey(size);
        final FieldMatrix<BigReal> cPublicKey = generator.generateCPublicKey(size);
        signer.sign(tempFile, signature, privateKey, aPublicKey);
        assertTrue(signer.verify(tempFile, signature, privateKey, cPublicKey));
    }

}
