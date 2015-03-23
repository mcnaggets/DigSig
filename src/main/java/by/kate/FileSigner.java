package by.kate;

import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;

public class FileSigner {

    private static final String BEGIN_SIGNATURE = "#Begin signature";
    private static final String END_SIGNATURE = "#End signature";

    private SignAlgorithm algorithm = new SignAlgorithm();

    public void sign(Path path, String signature, RealMatrix privateKey, RealMatrix publicKey) throws IOException {
        final double[][] encode = algorithm.encode(signature.getBytes(), publicKey, privateKey);
        final byte[] serialize = SerializationUtils.serialize(encode);

        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            bufferedWriter.newLine();
            bufferedWriter.write(BEGIN_SIGNATURE);
            bufferedWriter.newLine();
            bufferedWriter.write(Base64.getEncoder().encodeToString(serialize));
            bufferedWriter.newLine();
            bufferedWriter.write(END_SIGNATURE);
        }
    }

    public boolean verify(Path path, String signature, RealMatrix privateKey, RealMatrix publicKey) throws IOException {
        final List<String> lines = Files.readAllLines(path);
        final int begin = lines.indexOf(BEGIN_SIGNATURE);
        final int end = lines.indexOf(END_SIGNATURE);
        if (begin < 0 || end < 2) {
            throw new IllegalStateException();
        }
        final byte[] decode = Base64.getDecoder().decode(lines.get(begin + 1));
        final double[][] encodedMatrix = (double[][]) SerializationUtils.deserialize(decode);
        final byte[] decodedSignature = algorithm.decode(encodedMatrix, privateKey, publicKey);
        return signature.equals(new String(decodedSignature));
    }

}