package by.kate.sevice;

import org.apache.commons.math3.linear.FieldMatrix;
import org.apache.commons.math3.util.BigReal;
import org.apache.poi.POIXMLProperties;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class DocXFileSigner extends FileSigner {
    @Override
    public void sign(Path path, String signature, FieldMatrix<BigReal> privateKey, FieldMatrix<BigReal> publicKey) {
        final BigReal[][] encode = algorithmEncode(signature, privateKey, publicKey);
        final String base64Encode = base64Encode(serialize(encode));
        setSignatoryProperty(path, base64Encode);
    }

    private void setSignatoryProperty(Path path, String value) {
        try (final OPCPackage docPackage = OPCPackage.open(path.toFile())) {
            final POIXMLProperties properties = getProperties(docPackage);
            setSignatoryProperty(properties, value);
            properties.commit();
        } catch (XmlException | OpenXML4JException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setSignatoryProperty(POIXMLProperties properties, String value) throws IOException, OpenXML4JException, XmlException {
        final CTProperty signatoryProperty = getSignatoryProperty(properties);
        if (signatoryProperty == null) {
            properties.getCustomProperties().addProperty(SIGNATORY, value);
        } else {
            signatoryProperty.setLpwstr(value);
        }
    }

    @Override
    public void unSign(Path path) {
        setSignatoryProperty(path, null);
    }

    @Override
    Optional<String> getSignatory(Path path) {
        try (final OPCPackage docPackage = OPCPackage.open(path.toFile())) {
            final CTProperty signatory = getSignatoryProperty(getProperties(docPackage));
            return Optional.ofNullable(signatory).map(CTProperty::getLpwstr);
        } catch (XmlException | IOException | OpenXML4JException e) {
            throw new RuntimeException(e);
        }
    }

    private CTProperty getSignatoryProperty(POIXMLProperties properties) throws IOException, OpenXML4JException, XmlException {
        return properties.getCustomProperties().getProperty(SIGNATORY);
    }

    private POIXMLProperties getProperties(OPCPackage docPackage) throws IOException, OpenXML4JException, XmlException {
        return new POIXMLProperties(docPackage);
    }

    @Override
    public boolean canDisplayContent() {
        return false;
    }
}
