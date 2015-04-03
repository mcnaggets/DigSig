package by.kate.ui;

import by.kate.sevice.FileSigner;
import by.kate.sevice.KeyGenerator;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.commons.math3.linear.DefaultFieldMatrixChangingVisitor;
import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    public TextField signatory;
    public TextArea fileContent;
    public Label infoLabel;
    public Label filePath;

    private Optional<File> file = Optional.empty();

    private FileSigner signer = new FileSigner();
    private KeyGenerator generator = new KeyGenerator();

    private FieldMatrix<BigReal> publicKey;
    private FieldMatrix<BigReal> privateKey;
    private String storedSignatory;

    public void loadFile() {
        FileChooser chooser = new FileChooser();
        final File f = chooser.showOpenDialog(null);
        if (f != null) {
            changeKeys();
            showFile(f);
        }
    }

    private void showFile(File file) {
        try {
            this.file = Optional.of(file);
            filePath.setText(file.getPath());
            fileContent.setText(Files.readAllLines(file.toPath()).stream().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void signFile() {
        file.ifPresent(f -> {
            if (keysInvalid()) {
                changeKeys();
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
            if (verifyResult) {
                showInfo(Color.GREEN, "Подпись совпадает");
            } else {
                showInfo(Color.RED, "Подпись не совпадает");
            }
        });
    }

    private void showInfo(Color green, String text) {
        infoLabel.setTextFill(green);
        infoLabel.setText(text);
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
        file.ifPresent(f -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ключи");
            alert.setHeaderText("Ключи");
            alert.getDialogPane().setContent(new HBox(
                    new VBox(new Label("Закрытый ключ"), writeMatrix(privateKey)),
                    new VBox(new Label("Открытый ключ"), writeMatrix(publicKey))
            ));

            alert.showAndWait();
        });
    }

    private GridPane writeMatrix(FieldMatrix<BigReal> privateKey) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
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

    public void changeKeys() {
        generatePrivateKey();
        generatePublicKey();
        storedSignatory = signatory.getText();
        clearInfo();
    }

    private void clearInfo() {
        infoLabel.setText("");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            signatory.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!signatory.getText().equals(storedSignatory)) {
                    showInfo(Color.ORANGE, "Подпись изменена, необходимо изменить ключи");
                } else {
                    clearInfo();
                }
            });

            final Path temporaryFile = Files.createTempFile("temporary_file", ".txt");
            Files.write(temporaryFile, "Sample content".getBytes());
            changeKeys();
            showFile(temporaryFile.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
