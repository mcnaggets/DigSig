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
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.joining;

public class TextFileSigner extends FileSigner {

    private static final String BEGIN_SIGNATURE = "#Begin signature";
    private static final String END_SIGNATURE = "#End signature";

    @Override
    public void sign(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        final BigReal[][] encode = algorithmEncode(signature, privateKey, publicKey);
        final byte[] serialize = serialize(encode);

        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardOpenOption.APPEND)) {
            bufferedWriter.newLine();
            bufferedWriter.write(BEGIN_SIGNATURE);
            bufferedWriter.newLine();
            bufferedWriter.write(base64Encode(serialize));
            bufferedWriter.newLine();
            bufferedWriter.write(END_SIGNATURE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void unSign(Path path) {
        final Optional<Signatory> signatory = getSignatoryObject(path);
        signatory.ifPresent(s -> {
            List<String> unSigned = s.getLines();
            unSigned.remove(BEGIN_SIGNATURE);
            unSigned.remove(s.getText());
            unSigned.remove(END_SIGNATURE);
            writeToFile(path, unSigned);
        });
    }

    @Override
    public Optional<String> getSignatory(Path path) {
        return getSignatoryObject(path).map(Signatory::getText);
    }

    @Override
    public boolean canDisplayContent() {
        return true;
    }

    private Optional<Signatory> getSignatoryObject(Path path) {
        final List<String> lines = readAllLines(path);
        final int begin = lines.indexOf(BEGIN_SIGNATURE);
        final int end = lines.indexOf(END_SIGNATURE);
        if (begin < 0 || end < 2) {
            return Optional.empty();
        }
        String text = IntStream.range(begin + 1, end).mapToObj(lines::get).collect(joining());
        return Optional.of(new Signatory(begin, end, lines, text));
    }

}
