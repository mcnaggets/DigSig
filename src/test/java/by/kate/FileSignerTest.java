package by.kate;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;

public class FileSignerTest extends SignAlgorithmTest {

    private FileSigner signer = new FileSigner();

    private Path tempFile;

    @Before
    public void initialize() throws IOException {
        tempFile = Files.createTempFile("text", "txt");
        Files.write(tempFile, "temporary file content".getBytes());
    }

    @Test
    public void shouldSignFile() throws IOException {
        final String signature = "sign";
        signer.sign(tempFile, signature, t, a);
        assertTrue(signer.verify(tempFile, signature, t, c));
    }

}
