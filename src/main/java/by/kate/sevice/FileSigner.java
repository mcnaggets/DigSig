package by.kate.sevice;

import by.kate.model.Signatory;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class FileSigner {

    private static final String BEGIN_SIGNATURE = "#Begin signature";
    private static final String END_SIGNATURE = "#End signature";

    private SignAlgorithm algorithm = new SignAlgorithm();

    public void sign(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        final BigReal[][] encode = algorithm.encode(signature.getBytes(), publicKey, privateKey);
        final byte[] serialize = SerializationUtils.serialize(encode);

        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            bufferedWriter.newLine();
            bufferedWriter.write(BEGIN_SIGNATURE);
            bufferedWriter.newLine();
            bufferedWriter.write(Base64.getEncoder().encodeToString(serialize));
            bufferedWriter.newLine();
            bufferedWriter.write(END_SIGNATURE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void unSign(Path path) {
        final Optional<Signatory> signatory = getSignatory(path);
        signatory.ifPresent(s -> {
            List<String> unSigned = s.getLines();
            unSigned.remove(BEGIN_SIGNATURE);
            unSigned.remove(s.getText());
            unSigned.remove(END_SIGNATURE);
            writeToFile(path, unSigned);
        });
    }

    private void writeToFile(Path path, List<String> unSigned) {
        try {
            Files.write(path, unSigned);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean verify(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        final Optional<Signatory> signatory = getSignatory(path);
        if (signatory.isPresent()) {
            final byte[] decode = Base64.getDecoder().decode(signatory.get().getText());
            final BigReal[][] encodedMatrix = (BigReal[][]) SerializationUtils.deserialize(decode);
            final byte[] decodedSignature = algorithm.decode(encodedMatrix, privateKey, publicKey);
            return signature.equals(new String(decodedSignature));
        }
        return false;
    }

    private Optional<Signatory> getSignatory(Path path) {
        final List<String> lines = readAllLines(path);
        final int begin = lines.indexOf(BEGIN_SIGNATURE);
        final int end = lines.indexOf(END_SIGNATURE);
        if (begin < 0 || end < 2) {
            return Optional.empty();
        }
        String text = IntStream.range(begin + 1, end).mapToObj(lines::get).collect(joining());
        return Optional.of(new Signatory(begin, end, lines, text));
    }

    private List<String> readAllLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}