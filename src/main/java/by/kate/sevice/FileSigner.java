package by.kate.sevice;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.util.BigReal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public abstract class FileSigner {

    SignAlgorithm algorithm = new SignAlgorithm();

    static final Map<String, FileSigner> SIGNER_MAP = new HashMap<>();

    static final String SIGNATORY = "Signatory";

    static {
        SIGNER_MAP.put("text/plain", new TextFileSigner());
        SIGNER_MAP.put("application/msword", new DocFileSigner());
        SIGNER_MAP.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", new DocXFileSigner());
    }

    public static FileSigner getSigner(Path path) {
        try {
            return getFileSigner(path);
        } catch (Exception e) {
            return new NullFileSigner();
        }
    }

    private static FileSigner getFileSigner(Path path) throws IOException {
        final String contentType = Files.probeContentType(path);
        if (SIGNER_MAP.containsKey(contentType)) {
            return SIGNER_MAP.get(contentType);
        } else {
            return new NullFileSigner();
        }
    }

    public abstract void sign(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey);

    protected byte[] serialize(BigReal[][] encode) {
        return SerializationUtils.serialize(encode);
    }

    public abstract void unSign(Path path);

    public BigReal[][] algorithmEncode(String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        return algorithm.encode(signature.getBytes(), publicKey, privateKey);
    }

    public FieldMatrix<BigReal> generateSignature(String signature, FieldMatrix<BigReal> publicKey, FieldMatrix<BigReal> privateKey) {
        final BigReal[][] data = algorithmEncode(signature, privateKey, publicKey);
        return MatrixUtils.createFieldMatrix(data);
    }

    public boolean verify(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        final Optional<String> signatory = getSignatory(path);
        if (signatory.isPresent()) {
            final byte[] decode = base64Decode(signatory.get());
            final BigReal[][] encodedMatrix = deserialize(decode);
            final byte[] decodedSignature = algorithm.decode(encodedMatrix, privateKey, publicKey);
            return signature.equals(new String(decodedSignature));
        }
        return false;
    }

    BigReal[][] deserialize(byte[] decode) {
        return (BigReal[][]) SerializationUtils.deserialize(decode);
    }

    String base64Encode(byte[] serialize) {
        return Base64.getEncoder().encodeToString(serialize);
    }

    byte[] base64Decode(String signatory) {
        return Base64.getDecoder().decode(signatory);
    }

    List<String> readAllLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    void writeToFile(Path path, List<String> unSigned) {
        try {
            Files.write(path, unSigned);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    abstract Optional<String> getSignatory(Path path);

    public abstract boolean canDisplayContent();

    public boolean supports() {
        return true;
    }
}