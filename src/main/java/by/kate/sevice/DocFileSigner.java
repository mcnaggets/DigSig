package by.kate.sevice;

import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;
import org.apache.poi.hpsf.CustomProperties;
import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.HPSFPropertiesOnlyDocument;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Function;

public class DocFileSigner extends FileSigner {

    @Override
    public void sign(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        final BigReal[][] encode = algorithmEncode(signature, privateKey, publicKey);
        processDocument(path, document -> {
            final CustomProperties customProperties = getCustomProperties(document);
            customProperties.put(SIGNATORY, base64Encode(serialize(encode)));
            setCustomProperties(document, customProperties);
            return writeToTempFile(document);
        }).ifPresent(tmp -> moveFile(path, tmp));
    }

    private void setCustomProperties(HPSFPropertiesOnlyDocument document, CustomProperties customProperties) {
        getDocumentSummaryInformation(document).setCustomProperties(customProperties);
    }

    private void moveFile(Path path, Path tmp) {
        try {
            Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private CustomProperties getCustomProperties(HPSFPropertiesOnlyDocument document) {
        final DocumentSummaryInformation information = getDocumentSummaryInformation(document);
        final CustomProperties properties = information.getCustomProperties();
        if (properties == null) {
            return new CustomProperties();
        }
        return properties;
    }

    private DocumentSummaryInformation getDocumentSummaryInformation(HPSFPropertiesOnlyDocument document) {
        final DocumentSummaryInformation information = document.getDocumentSummaryInformation();
        if (information == null) {
            document.createInformationProperties();
            return document.getDocumentSummaryInformation();
        }
        return information;
    }

    private Optional<Path> writeToTempFile(HPSFPropertiesOnlyDocument document) {
        try {
            final Path tempFile = Files.createTempFile("tmp_doc", ".document");
            writeDocument(document, tempFile);
            return Optional.of(tempFile);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void writeDocument(HPSFPropertiesOnlyDocument doc, Path tempFile) {
        try (FileOutputStream out = new FileOutputStream(tempFile.toFile())) {
            doc.write(out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void unSign(Path path) {
        processDocument(path, document -> {
            DocumentSummaryInformation si = getDocumentSummaryInformation(document);
            si.getCustomProperties().remove(SIGNATORY);
            return writeToTempFile(document);
        }).ifPresent(tmp -> moveFile(path, tmp));
    }

    @Override
    Optional<String> getSignatory(Path path) {
        return processDocument(path, document -> {
            DocumentSummaryInformation si = getDocumentSummaryInformation(document);
            final String signatory = (String) si.getCustomProperties().get(SIGNATORY);
            return Optional.ofNullable(signatory);
        });
    }

    @Override
    public boolean canDisplayContent() {
        return false;
    }

    private <T> Optional<T> processDocument(Path path, Function<HPSFPropertiesOnlyDocument, Optional<T>> documentFunction) {
        try (NPOIFSFileSystem fileSystem = new NPOIFSFileSystem(path.toFile())) {
            return documentFunction.apply(new HPSFPropertiesOnlyDocument(fileSystem));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
