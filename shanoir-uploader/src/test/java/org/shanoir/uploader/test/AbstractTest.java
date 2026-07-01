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
import java.util.Arrays;
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
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.ShUpOnloadConfig;
import org.shanoir.uploader.ShanoirUploader;
import org.shanoir.uploader.dicom.anonymize.Pseudonymizer;
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
import org.shanoir.uploader.model.rest.StudyUser;
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

    private static final String ADMIN_NAME = "shanoir.client.admin.name";

    private static final String ADMIN_PASSWORD = "shanoir.client.admin.password";

    private static final String EXPERT_NAME = "shanoir.client.expert.name";

    private static final String EXPERT_PASSWORD = "shanoir.client.expert.password";

    private static final String USER_NAME = "shanoir.client.user.name";

    private static final String USER_PASSWORD = "shanoir.client.user.password";

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
        String name = resolveCredential(nameKey);
        String password = resolveCredential(passwordKey);
        if (StringUtils.isBlank(name) || StringUtils.isBlank(password)) {
            LOG.warn("Credentials for {} not configured (keys: {}, {}). Client will be null.",
                    roleLabel, nameKey, passwordKey);
            return null;
        }
        ShanoirUploaderServiceClient client = new ShanoirUploaderServiceClient();
        client.configure();
        try {
            String accessToken = client.loginWithKeycloakForToken(name, password);
            if (accessToken == null) {
                LOG.error("Login failed for {} (user={}). Server down or wrong credentials.", roleLabel, name);
                return null;
            }
            client.setAccessToken(accessToken);
            LOG.info("Authenticated {} as user {}, {}.", roleLabel, name, client.getUserId());
            return client;
        } catch (Exception e) {
            LOG.error("Exception while authenticating {}: {}", roleLabel, e.getMessage());
            return null;
        }
    }

    /**
     * Resolves a credential value, giving priority to an environment variable
     * over the corresponding entry in {@code test.properties}.
     *
     * The environment variable name is derived from the property key by
     * upper-casing it and replacing every {@code '.'} with {@code '_'}. For
     * example, the property key {@code "user.name"} maps to the environment
     * variable {@code USER_NAME}, and {@code "user.password"} maps to
     * {@code USER_PASSWORD}.
     *
     * This allows CI/CD pipelines or container deployments to inject
     * credentials (e.g. from secrets) via environment variables, without
     * requiring {@code test.properties} to be present or populated. When the
     * environment variable is not set (or blank), the value falls back to
     * {@code test.properties}.
     *
     * @param propertyKey the dotted property key (e.g. {@code "admin.password"})
     * @return the resolved value, or {@code null}/blank if neither source has it
     */
    private static String resolveCredential(String propertyKey) {
        String envVarName = propertyKey.toUpperCase().replace('.', '_');
        String envValue = System.getenv(envVarName);
        if (StringUtils.isNotBlank(envValue)) {
            LOG.debug("Resolved credential for key '{}' from environment variable '{}'.",
                    propertyKey, envVarName);
            return envValue;
        }
        return testProperties.getProperty(propertyKey);
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
        Study study = buildMinimalStudy();

        List<StudyCenter> studyCenterList = new ArrayList<>();
        StudyCenter studyCenter = new StudyCenter();
        Center createdCenter = createCenter();
        Assertions.assertNotNull(createdCenter);
        studyCenter.setCenter(createdCenter);
        studyCenterList.add(studyCenter);
        study.setStudyCenterList(studyCenterList);

        study = adminClient.createStudy(study);
        Assertions.assertNotNull(study);
        LOG.info("New study {} ({}) created.", study.getName(), study.getId());

        StudyUser studyUser = new StudyUser();
        studyUser.setStudyId(study.getId());
        studyUser.setUserId(expertClient.getUserId());
        studyUser.setUserName(expertClient.getUserName());
        studyUser.setConfirmed(true);
        studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_SEE_ALL, StudyUserRight.CAN_DOWNLOAD,
                    StudyUserRight.CAN_IMPORT, StudyUserRight.CAN_ADMINISTRATE));
        studyUser = adminClient.addStudyUser(study.getId(), studyUser);
        Assertions.assertNotNull(studyUser);
        LOG.info("StudyUser {} added to study: {}", studyUser.getUserName(), study.getName());

        AcquisitionEquipment createdEquipment = createEquipment(createdCenter);
        Assertions.assertNotNull(createdEquipment);

        StudyCard studyCard = new StudyCard();
        studyCard.setName("Study-Card-" + UUID.randomUUID());
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

    /**
     * Builds a minimal valid {@link Study} payload suitable for a POST to the
     * studies endpoint. Mirrors the structure used in
     * {@link AbstractTest#createStudyAndCenterAndStudyCard()} but deliberately
     * omits the study-card (not needed for the approval flow under test).
     */
    public static Study buildMinimalStudy() {
        StudyExtraDetails extraDetails = new StudyExtraDetails();
        extraDetails.setExpectedNbOfSubjects(5L);
        extraDetails.setExpectedNbOfCenters(1L);
        extraDetails.setSponsor("Test-Sponsor");
        extraDetails.setPrincipalInvestigator("Test-Principal-Investigator");

        Study study = new Study();
        study.setExtraDetails(extraDetails);
        study.setName("Study-" + UUID.randomUUID());
        // isDraft will be overridden server-side for non-admin callers, but we
        // set it explicitly to make the intention of this test clear.
        study.setIsDraft(Boolean.FALSE);
        study.setStudyStatus(IN_PROGRESS);
        study.setStudyCardPolicy(Study.SC_MANDATORY);

        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.YEAR, 1);
        study.setStartDate(today);
        study.setEndDate(cal.getTime());

        // StudyCenterList can be empty for the draft-approval scenario;
        // the server will accept the study and assign it draft status.
        study.setStudyCenterList(new java.util.ArrayList<>());

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


    /**
     * Builds a {@link StudyUser} candidate for the given study and user ids with
     * a standard set of import/view rights.
     *
     * @param studyId the target study
     * @param userId  the user to add
     * @return a populated but not-yet-persisted {@link StudyUser}
     */
    public StudyUser buildStudyUser(Long studyId, Long userId, String userName) {
        StudyUser su = new StudyUser();
        su.setStudyId(studyId);
        su.setUserId(userId);
        su.setUserName(userName);
        su.setConfirmed(true);
        su.setStudyUserRights(Arrays.asList(
                StudyUserRight.CAN_SEE_ALL,
                StudyUserRight.CAN_DOWNLOAD,
                StudyUserRight.CAN_IMPORT));
        return su;
    }

}