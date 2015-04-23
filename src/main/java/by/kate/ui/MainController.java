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
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    public TextField signatory;
    public TextArea fileContent;
    public Label infoLabel;
    public Label filePath;

    private Optional<File> file = Optional.empty();
    private FieldMatrix<BigReal> publicKey;
    private FieldMatrix<BigReal> privateKey;

    private KeyGenerator generator = new KeyGenerator();

    private String storedSignatory;

    public void loadFile() {
        onLoadFile(this::showFile);
    }

    private void showFile(File file) {
        try {
            this.file = Optional.of(file);
            filePath.setText(file.getPath());
            final FileSigner fileSigner = getFileSigner(file.toPath());
            if (fileSigner.canDisplayContent()) {
                fileContent.setText(Files.readAllLines(file.toPath()).stream().collect(Collectors.joining("\n")));
                fileContent.setEditable(true);
            } else {
                fileContent.setText("Не возможно отобразить файл");
                fileContent.setEditable(false);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void signFile() {
        file.ifPresent(f -> {
            if (keysInvalid()) {
                invalidKeys();
                return;
            }
            signFile(f);
            showFile(f);
        });
    }

    private void invalidKeys() {
        showInfo(Color.RED, "Ключи не соответствуют подписи");
    }

    private void signFile(File f) {
        final Path path = f.toPath();
        final FileSigner signer = getFileSigner(path);
        signer.sign(path, getSignatory(), privateKey, publicKey);
    }

    private FileSigner getFileSigner(Path path) {
        return FileSigner.getSigner(path);
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
            final boolean verifyResult = !keysInvalid() && getFileSigner(f.toPath()).verify(f.toPath(), getSignatory(), privateKey, publicKey);
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
            getFileSigner(f.toPath()).unSign(f.toPath());
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

    public void showMatrix(FieldMatrix<BigReal> matrix, String label) {
        file.ifPresent(f -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(label);
            alert.setHeaderText(label);
            alert.getDialogPane().setContent(writeMatrix(matrix));

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

    private void clearInfo() {
        storedSignatory = signatory.getText();
        infoLabel.setText("");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            addSignatoryListener();
            generatePublicKey();
            generatePrivateKey();
            createAndShowTemporaryFile();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void createAndShowTemporaryFile() throws IOException {
        final Path temporaryFile = Files.createTempFile("temporary_file", ".txt");
        Files.write(temporaryFile, "Sample content".getBytes());
        showFile(temporaryFile.toFile());
    }

    private void addSignatoryListener() {
        signatory.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!signatory.getText().equals(storedSignatory)) {
                signatoryChanged();
            } else {
                clearInfo();
            }
        });
    }

    private void signatoryChanged() {
        showInfo(Color.ORANGE, "Подпись изменена, необходимо изменить ключи");
    }

    public void showSignatory() {
        final FieldMatrix<BigReal> signatory = getFileSigner(null).generateSignature(getSignatory(), publicKey, privateKey);
        showMatrix(signatory, "Подпись");
    }

    public void loadPrivateKey() throws IOException {
        onLoadFile(this::readPrivateKeyFromFile);
    }

    public void loadPublicKey() throws IOException {
        onLoadFile(this::readPublicKeyFromFile);
    }

    private void readPrivateKeyFromFile(File f) {
        try {
            privateKey = generator.readFromFile(f.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void readPublicKeyFromFile(File f) {
        try {
            publicKey = generator.readFromFile(f.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void onLoadFile(Consumer<File> consumer) {
        FileChooser chooser = new FileChooser();
        final File f = chooser.showOpenDialog(null);
        if (f != null) {
            consumer.accept(f);
        }
    }

    private void onFileSave(Consumer<File> consumer) {
        FileChooser chooser = new FileChooser();
        final File f = chooser.showSaveDialog(null);
        if (f != null) {
            consumer.accept(f);
        }
    }

    public void showPrivateKey() {
        showMatrix(privateKey, "Частный ключ");
    }

    public void showPublicKey() {
        showMatrix(publicKey, "Открытый ключ");
    }

    public void savePrivateKey() {
        onFileSave(this::savePrivateKey);
    }

    private void savePrivateKey(File f) {
        try {
            generator.writeToFile(f.toPath(), privateKey);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void savePublicKey(File f) {
        try {
            generator.writeToFile(f.toPath(), publicKey);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void savePublicKey() {
        onFileSave(this::savePublicKey);
    }
}
