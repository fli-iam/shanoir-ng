package org.shanoir.uploader.test;

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.shanoir.ng.exchange.imports.subject.IdentifierCalculator;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.ShanoirUploader;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
import org.shanoir.uploader.exception.PseudonymusException;
import org.shanoir.uploader.model.rest.AcquisitionEquipment;
import org.shanoir.uploader.model.rest.Center;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.Manufacturer;
import org.shanoir.uploader.model.rest.ManufacturerModel;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the base class for all ShUp test classes, that do
 * integration tests on shanoir servers. It provides the login and
 * the creation of the ShanoirUploaderServiceClient.
 *
 * @author mkain
 *
 */
public abstract class AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

    private static final String TEST_PROPERTIES = "test.properties";

    public static Properties testProperties = new Properties();

    private static final String PROFILE = "profile";

    private static final String USER_NAME = "user.name";

    private static final String USER_PASSWORD = "user.password";

    protected static ShanoirUploaderServiceClient shUpClient;

    protected static Pseudonymizer pseudonymizer;

    protected static IdentifierCalculator identifierCalculator;

    @BeforeAll
    public static void setup() {
        ShanoirUploader.initShanoirUploaderFolders();
        PropertiesUtil.initPropertiesFromResourcePath(testProperties, TEST_PROPERTIES);
        PropertiesUtil.initPropertiesFromResourcePath(ShUpConfig.profileProperties, ShUpConfig.PROFILE_DIR + testProperties.getProperty(PROFILE) + "/" + ShUpConfig.PROFILE_PROPERTIES);
        PropertiesUtil.initPropertiesFromResourcePath(ShUpConfig.endpointProperties, ShUpConfig.ENDPOINT_PROPERTIES);
        identifierCalculator = new IdentifierCalculator();
        shUpClient = new ShanoirUploaderServiceClient();
        shUpClient.configure();
        ShUpOnloadConfig.setShanoirUploaderServiceClient(shUpClient);
        String user = testProperties.getProperty(USER_NAME);
        String password = testProperties.getProperty(USER_PASSWORD);
        String token;
        try {
            token = shUpClient.loginWithKeycloakForToken(user, password);
            if (token != null) {
                ShUpOnloadConfig.setTokenString(token);
            } else {
                LOG.error("ERROR: login not successful.");
                Assumptions.assumeTrue(false, "Skipping test: probably no server available.");
            }
            if (ShUpConfig.isModePseudonymus()) {
                File pseudonymusFolder = new File(ShUpOnloadConfig.getWorkFolder().getParentFile().getAbsolutePath() + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
                String pseudonymusKeyValue = shUpClient.findValueByKey(ShUpConfig.MODE_PSEUDONYMUS_KEY);
                try {
                    pseudonymizer = new Pseudonymizer(pseudonymusKeyValue, pseudonymusFolder.getAbsolutePath());
                } catch (PseudonymusException e) {
                    LOG.error(e.getMessage(), e);
                    System.exit(0);
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            Assumptions.assumeTrue(false, "Skipping test: probably no server available.");
        }
    }

    public static Center createCenter() {
        Center center = new Center();
        String centerUUID = UUID.randomUUID().toString();
        center.setName("Center-Name-" + centerUUID);
        center.setCity("Rennes");
        center.setStreet("Center-Street-" + centerUUID);
        center.setCountry("Center-Country-" + centerUUID);
        center.setPostalCode("35000");
        center.setWebsite("Center-Website-" + centerUUID);
        center.setPhoneNumber("+3335353535");
        Center createdCenter = shUpClient.createCenter(center);
        return createdCenter;
    }

    public static AcquisitionEquipment createEquipment(Center createdCenter) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName("Manufacturer-" + UUID.randomUUID().toString());
        Manufacturer createdManufacturer = shUpClient.createManufacturer(manufacturer);
        Assertions.assertNotNull(createdManufacturer);
        ManufacturerModel manufacturerModel = new ManufacturerModel();
        manufacturerModel.setName("Manufacturer-Model-" + UUID.randomUUID().toString());
        manufacturerModel.setManufacturer(createdManufacturer);
        manufacturerModel.setDatasetModalityType("0"); // 0 == MR
        manufacturerModel.setMagneticField(3.0);
        ManufacturerModel createdManufacturerModel = shUpClient.createManufacturerModel(manufacturerModel);
        Assertions.assertNotNull(createdManufacturerModel);
        AcquisitionEquipment equipment = new AcquisitionEquipment();
        String serialNumberRandom = "Serial-Number-" + UUID.randomUUID().toString();
        equipment.setSerialNumber(serialNumberRandom);
        equipment.setCenter(new IdName(createdCenter.getId(), createdCenter.getName()));
        equipment.setManufacturerModel(createdManufacturerModel);
        AcquisitionEquipment createdEquipment = shUpClient.createEquipment(equipment);
        return createdEquipment;
    }

}
