package by.kate.sevice;

import by.kate.model.Signatory;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;

import java.nio.file.Path;
import java.util.Optional;

public class NullFileSigner extends FileSigner {

    @Override
    public void sign(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        // do nothing
    }

    @Override
    public void unSign(Path path) {
        // do nothing
    }

    @Override
    Optional<String> getSignatory(Path path) {
        return Optional.empty();
    }

    @Override
    public boolean canDisplayContent() {
        return false;
    }

}
