package by.kate.ui;

import by.kate.sevice.FileSigner;
import by.kate.sevice.KeyGenerator;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.apache.commons.math3.linear.DefaultFieldMatrixChangingVisitor;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {

    public TextField signatory;
    public TextArea fileContent;
    public Label verify;

    private Optional<File> file;

    private FileSigner signer = new FileSigner();
    private KeyGenerator generator = new KeyGenerator();

    private FieldMatrix<BigReal> publicKey;
    private FieldMatrix<BigReal> privateKey;

    public void loadFile() {
        FileChooser chooser = new FileChooser();
        file = Optional.ofNullable(chooser.showOpenDialog(null));
        file.ifPresent(f -> {
            generatePrivateKey();
            generatePublicKey();
            showFile(f);
        });
    }

    private void showFile(File file) {
        try {
            fileContent.setText(Files.readAllLines(file.toPath()).stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void signFile() {
        file.ifPresent(f -> {
            if (keysInvalid()) {
                generatePrivateKey();
                generatePublicKey();
            }
            signer.sign(f.toPath(), getSignatory(), privateKey, publicKey);
            showFile(f);
        });
    }

    private boolean keysInvalid() {
        return getLength() != privateKey.getRowDimension()
                || getLength() != publicKey.getRowDimension();
    }

    private String getSignatory() {
        return signatory.getText();
    }

    public void generatePublicKey() {
        publicKey = generator.generateAPublicKey(getLength());
    }

    private int getLength() {
        return getSignatory().getBytes().length;
    }

    public void generatePrivateKey() {
        privateKey = generator.generateTPrivateKey(getLength());
    }

    public void verifyFile() {
        file.ifPresent(f -> {
            final FieldMatrix<BigReal> publicKey = generator.generateCPublicKey(getLength());
            final boolean verifyResult = !keysInvalid() && signer.verify(f.toPath(), getSignatory(), privateKey, publicKey);
            verify.setText(verifyResult ? "Подпись совпадает" : "Подпись не совпадает");
        });
    }

    public void unSignFile() {
        file.ifPresent(f -> {
            signer.unSign(f.toPath());
            showFile(f);
        });
    }

    public void saveFile() {
        file.ifPresent(f -> {
            try {
                Files.write(f.toPath(), Arrays.asList(fileContent.getText().split("\n")));
                showFile(f);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    public void showKeys() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ключи");
        alert.setHeaderText("Ключи");
        alert.getDialogPane().setContent(new HBox(
                writeMatrix(privateKey),
                writeMatrix(publicKey)
        ));

        alert.showAndWait();
    }

    private GridPane writeMatrix(FieldMatrix<BigReal> privateKey) {
        GridPane grid = new GridPane();
        grid.setStyle("-fx-border: 2px solid; -fx-background-color: palegreen; -fx-padding: 4; -fx-hgap: 4; -fx-vgap: 4;");
        grid.setSnapToPixel(false);
        privateKey.walkInOptimizedOrder(new DefaultFieldMatrixChangingVisitor<BigReal>(BigReal.ZERO) {
            @Override
            public BigReal visit(int row, int column, BigReal value) {
                grid.add(new Label(String.valueOf(value.bigDecimalValue().intValue())), column, row);
                return super.visit(row, column, value);
            }
        });
        return grid;
    }

}
