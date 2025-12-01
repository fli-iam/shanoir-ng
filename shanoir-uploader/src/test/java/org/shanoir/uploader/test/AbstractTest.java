/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.uploader.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.StudyCenter;
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

    private static final String IN_PROGRESS = "IN_PROGRESS";

    protected static ShanoirUploaderServiceClient shUpClient;

    protected static Pseudonymizer pseudonymizer;

    protected static IdentifierCalculator identifierCalculator;

    @BeforeAll
    public static void setup() {
        ShanoirUploader.initShanoirUploaderFolders();
        PropertiesUtil.initPropertiesFromResourcePath(testProperties, TEST_PROPERTIES);
        PropertiesUtil.initPropertiesFromResourcePath(ShUpConfig.profileProperties,
                ShUpConfig.PROFILE_DIR + testProperties.getProperty(PROFILE) + "/" + ShUpConfig.PROFILE_PROPERTIES);
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
                File pseudonymusFolder = new File(ShUpOnloadConfig.getWorkFolder().getParentFile().getAbsolutePath()
                        + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
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

    public static org.shanoir.uploader.model.rest.Study createStudyAndCenterAndStudyCard() {
        org.shanoir.uploader.model.rest.Study study = new org.shanoir.uploader.model.rest.Study();
        final String randomStudyName = "Study-Name-" + UUID.randomUUID().toString();
        study.setName(randomStudyName);
        study.setStudyStatus(IN_PROGRESS);
        study.setStudyCardPolicy(org.shanoir.uploader.model.rest.Study.SC_MANDATORY);
        // add center to study
        List<StudyCenter> studyCenterList = new ArrayList<StudyCenter>();
        final StudyCenter studyCenter = new StudyCenter();
        Center createdCenter = createCenter();
        Assertions.assertNotNull(createdCenter);
        studyCenter.setCenter(createdCenter);
        studyCenterList.add(studyCenter);
        study.setStudyCenterList(studyCenterList);
        // create study
        study = shUpClient.createStudy(study);
        Assertions.assertNotNull(study);
        // create equipment
        AcquisitionEquipment createdEquipment = createEquipment(createdCenter);
        Assertions.assertNotNull(createdEquipment);
        // create study card and add to study
        StudyCard studyCard = new StudyCard();
        final String randomStudyCardName = "Study-Card-Name-" + UUID.randomUUID().toString();
        studyCard.setName(randomStudyCardName);
        studyCard.setAcquisitionEquipmentId(createdEquipment.getId());
        studyCard.setAcquisitionEquipment(createdEquipment);
        studyCard.setCenterId(createdCenter.getId());
        studyCard.setStudyId(study.getId());
        shUpClient.createStudyCard(studyCard);
        Assertions.assertNotNull(studyCard);
        List<StudyCard> studyCards = new ArrayList<>();
        studyCards.add(studyCard);
        study.setStudyCards(studyCards);
        return study;
    }

}
