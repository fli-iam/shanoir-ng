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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
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
import org.shanoir.uploader.model.rest.Examination;
import org.shanoir.uploader.model.rest.HemisphericDominance;
import org.shanoir.uploader.model.rest.IdName;
import org.shanoir.uploader.model.rest.ImagedObjectCategory;
import org.shanoir.uploader.model.rest.Manufacturer;
import org.shanoir.uploader.model.rest.ManufacturerModel;
import org.shanoir.uploader.model.rest.Sex;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyCard;
import org.shanoir.uploader.model.rest.StudyCenter;
import org.shanoir.uploader.model.rest.StudyExtraDetails;
import org.shanoir.uploader.model.rest.Subject;
import org.shanoir.uploader.model.rest.SubjectType;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the base class for all ShUp test classes, that do
 * integration tests on shanoir servers. It provides login and creation of
 * role-specific ShanoirUploaderServiceClient instances.
 *
 * Three roles are supported, each backed by its own authenticated client:
 * - adminClient (ROLE_ADMIN)
 * - expertClient (ROLE_EXPERT)
 * - userClient (ROLE_USER)
 *
 * @author mkain
 */
public abstract class AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

    private static final String TEST_PROPERTIES = "test.properties";

    public static Properties testProperties = new Properties();

    private static final String PROFILE = "profile";

    private static final String ADMIN_NAME = "admin.name";

    private static final String ADMIN_PASSWORD = "admin.password";

    private static final String EXPERT_NAME = "expert.name";

    private static final String EXPERT_PASSWORD = "expert.password";

    private static final String USER_NAME = "user.name";

    private static final String USER_PASSWORD = "user.password";

    private static final String IN_PROGRESS = "IN_PROGRESS";

    // -------------------------------------------------------------------------
    // Role-specific authenticated clients
    // -------------------------------------------------------------------------
    protected static ShanoirUploaderServiceClient adminClient;

    protected static ShanoirUploaderServiceClient expertClient;

    protected static ShanoirUploaderServiceClient userClient;

    protected static Pseudonymizer pseudonymizer;

    protected static IdentifierCalculator identifierCalculator;

    @BeforeAll
    public static void setup() {
        ShanoirUploader.initShanoirUploaderFolders();
        PropertiesUtil.initPropertiesFromResourcePath(testProperties, TEST_PROPERTIES);

        String profile = testProperties.getProperty(PROFILE);
        PropertiesUtil.initPropertiesFromResourcePath(ShUpConfig.profileProperties,
                ShUpConfig.PROFILE_DIR + profile + "/" + ShUpConfig.PROFILE_PROPERTIES);
        PropertiesUtil.initPropertiesFromResourcePath(ShUpConfig.endpointProperties, ShUpConfig.ENDPOINT_PROPERTIES);

        identifierCalculator = new IdentifierCalculator();

        // Log in each role; a missing credentials entry is treated as "not configured"
        // rather than a hard failure so that partial test environments still work.
        adminClient = buildAuthenticatedClient(ADMIN_NAME, ADMIN_PASSWORD, "ROLE_ADMIN");
        expertClient = buildAuthenticatedClient(EXPERT_NAME, EXPERT_PASSWORD, "ROLE_EXPERT");
        userClient = buildAuthenticatedClient(USER_NAME, USER_PASSWORD, "ROLE_USER");

        // At least one client must have authenticated successfully
        boolean anyClientAvailable = (adminClient != null || expertClient != null || userClient != null);
        Assumptions.assumeTrue(anyClientAvailable,
                "Skipping tests: no role credentials configured or server unavailable.");

        ShanoirUploaderServiceClient defaultClient = expertClient != null ? expertClient
                : adminClient != null ? adminClient : userClient;
        ShUpOnloadConfig.setShanoirUploaderServiceClient(defaultClient);

        // Pseudonymizer – only needed when the mode is active
        if (ShUpConfig.isModePseudonymus() && defaultClient != null) {
            File pseudonymusFolder = new File(ShUpOnloadConfig.getWorkFolder().getParentFile().getAbsolutePath()
                    + File.separator + Pseudonymizer.PSEUDONYMUS_FOLDER);
            try {
                String pseudonymusKeyValue = defaultClient.findValueByKey(ShUpConfig.MODE_PSEUDONYMUS_KEY);
                pseudonymizer = new Pseudonymizer(pseudonymusKeyValue, pseudonymusFolder.getAbsolutePath());
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                System.exit(0);
            }
        }
    }

    /**
     * Creates and authenticates a {@link ShanoirUploaderServiceClient} for the
     * given credentials. Returns {@code null} when credentials are blank or when
     * the server cannot be reached, so callers can skip gracefully with
     * {@link Assumptions}.
     *
     * @param nameKey     property key for the username (e.g. {@code "expert.name"})
     * @param passwordKey property key for the password (e.g.
     *                    {@code "expert.password"})
     * @param roleLabel   human-readable label used only in log messages
     * @return authenticated client, or {@code null} on failure
     */
    private static ShanoirUploaderServiceClient buildAuthenticatedClient(
            String nameKey, String passwordKey, String roleLabel) {
        String name = testProperties.getProperty(nameKey);
        String password = testProperties.getProperty(passwordKey);
        if (StringUtils.isBlank(name) || StringUtils.isBlank(password)) {
            LOG.warn("Credentials for {} not configured (keys: {}, {}). Client will be null.",
                    roleLabel, nameKey, passwordKey);
            return null;
        }
        ShanoirUploaderServiceClient client = new ShanoirUploaderServiceClient();
        client.configure();
        try {
            String token = client.loginWithKeycloakForToken(name, password);
            if (token == null) {
                LOG.error("Login failed for {} (user={}). Server down or wrong credentials.", roleLabel, name);
                return null;
            }
            ShUpOnloadConfig.setTokenString(token);
            LOG.info("Authenticated {} as user {}.", roleLabel, name);
            return client;
        } catch (Exception e) {
            LOG.error("Exception while authenticating {}: {}", roleLabel, e.getMessage());
            return null;
        }
    }

    protected static void requireAdminClient() {
        Assumptions.assumeTrue(adminClient != null,
                "Skipping test: ROLE_ADMIN client not available.");
    }

    protected static void requireExpertClient() {
        Assumptions.assumeTrue(expertClient != null,
                "Skipping test: ROLE_EXPERT client not available.");
    }

    protected static void requireUserClient() {
        Assumptions.assumeTrue(userClient != null,
                "Skipping test: ROLE_USER client not available.");
    }

    public static Center createCenter() {
        String centerUUID = UUID.randomUUID().toString();
        Center center = new Center();
        center.setName("Center-Name-" + centerUUID);
        center.setCity("Rennes");
        center.setStreet("Center-Street-" + centerUUID);
        center.setCountry("Center-Country-" + centerUUID);
        center.setPostalCode("35000");
        center.setWebsite("Center-Website-" + centerUUID);
        center.setPhoneNumber("+3335353535");
        return expertClient.createCenter(center);
    }

    public static AcquisitionEquipment createEquipment(Center createdCenter) {
        Manufacturer manufacturer = new Manufacturer();
        manufacturer.setName("Manufacturer-" + UUID.randomUUID());
        Manufacturer createdManufacturer = expertClient.createManufacturer(manufacturer);
        Assertions.assertNotNull(createdManufacturer);

        ManufacturerModel manufacturerModel = new ManufacturerModel();
        manufacturerModel.setName("Manufacturer-Model-" + UUID.randomUUID());
        manufacturerModel.setManufacturer(createdManufacturer);
        manufacturerModel.setDatasetModalityType("0"); // 0 == MR
        manufacturerModel.setMagneticField(3.0);
        ManufacturerModel createdManufacturerModel = expertClient.createManufacturerModel(manufacturerModel);
        Assertions.assertNotNull(createdManufacturerModel);

        AcquisitionEquipment equipment = new AcquisitionEquipment();
        equipment.setSerialNumber("Serial-Number-" + UUID.randomUUID());
        equipment.setCenter(new IdName(createdCenter.getId(), createdCenter.getName()));
        equipment.setManufacturerModel(createdManufacturerModel);
        return expertClient.createEquipment(equipment);
    }

    public static Study createStudyAndCenterAndStudyCard() {
        StudyExtraDetails studyExtraDetails = new StudyExtraDetails();
        studyExtraDetails.setExpectedNbOfSubjects(5L);
        studyExtraDetails.setExpectedNbOfCenters(5L);
        studyExtraDetails.setSponsor("sponsor");
        studyExtraDetails.setPrincipalInvestigator("principal investigator");

        Study study = new Study();
        study.setExtraDetails(studyExtraDetails);
        study.setName("Study-Name-" + UUID.randomUUID());
        study.setIsDraft(Boolean.TRUE);

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.YEAR, 1);
        study.setStartDate(today);
        study.setEndDate(calendar.getTime());
        study.setStudyStatus(IN_PROGRESS);
        study.setStudyCardPolicy(Study.SC_MANDATORY);

        List<StudyCenter> studyCenterList = new ArrayList<>();
        StudyCenter studyCenter = new StudyCenter();
        Center createdCenter = createCenter();
        Assertions.assertNotNull(createdCenter);
        studyCenter.setCenter(createdCenter);
        studyCenterList.add(studyCenter);
        study.setStudyCenterList(studyCenterList);

        study = adminClient.createStudy(study);
        Assertions.assertNotNull(study);

        AcquisitionEquipment createdEquipment = createEquipment(createdCenter);
        Assertions.assertNotNull(createdEquipment);

        StudyCard studyCard = new StudyCard();
        studyCard.setName("Study-Card-Name-" + UUID.randomUUID());
        studyCard.setAcquisitionEquipmentId(createdEquipment.getId());
        studyCard.setAcquisitionEquipment(createdEquipment);
        studyCard.setCenterId(createdCenter.getId());
        studyCard.setStudyId(study.getId());
        expertClient.createStudyCard(studyCard);
        Assertions.assertNotNull(studyCard);

        List<StudyCard> studyCards = new ArrayList<>();
        studyCards.add(studyCard);
        study.setStudyCards(studyCards);
        return study;
    }

    public Subject createSubject(Study study) {
        Subject subject = new Subject();
        String randomPatientName = UUID.randomUUID().toString().substring(0, 15).replaceAll("-", "");
        subject.setName(randomPatientName);
        subject.setStudy(new IdName(study.getId(), study.getName()));
        subject.setBirthDate(LocalDate.now());
        subject.setSex(Sex.O);
        subject.setImagedObjectCategory(ImagedObjectCategory.LIVING_HUMAN_BEING);
        subject.setLanguageHemisphericDominance(HemisphericDominance.Left);
        subject.setManualHemisphericDominance(HemisphericDominance.Left);
        subject.setSubjectType(SubjectType.PATIENT);
        subject.setPhysicallyInvolved(true);
        subject.setTags(new ArrayList<>());
        return expertClient.createSubject(subject, true, null);
    }

    public Examination createExamination(Long studyId, Long subjectId, Long centerId) {
        Examination examination = new Examination();
        examination.setStudyId(studyId);
        examination.setSubjectId(subjectId);
        examination.setCenterId(centerId);
        examination.setExaminationDate(new Date());
        examination.setComment("examinationComment");
        return expertClient.createExamination(examination);
    }

}