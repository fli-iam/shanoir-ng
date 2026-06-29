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

package org.shanoir.uploader.test.study;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.uploader.model.rest.Study;
import org.shanoir.uploader.model.rest.StudyExtraDetails;
import org.shanoir.uploader.model.rest.StudyUser;
import org.shanoir.uploader.service.rest.ShanoirUploaderServiceClient;
import org.shanoir.uploader.test.AbstractTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for the study draft approval workflow and study-user
 * management.
 *
 * Scenario (executed in order):
 * 1. Expert creates a new study  → study lands in DRAFT state (isDraft=true).
 * 2. Expert tries to add a member → must fail (draft studies reject new
 *    memberships until approved).
 * 3. Admin approves the draft study → study transitions to non-draft.
 * 4. Expert adds the same member again → must succeed now.
 *
 * Both adminClient and expertClient must be available; the test suite is
 * skipped gracefully when either is absent (e.g. partial CI environment).
 *
 * @author mkain
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StudyDraftTest extends AbstractTest {

    private static final Logger LOG = LoggerFactory.getLogger(StudyDraftTest.class);

    private static final String IN_PROGRESS = "IN_PROGRESS";

    /**
     * The study created in step 1 and shared across all steps via a static field
     * so that ordered test methods can hand state to each other without a full
     * setup/teardown cycle.
     */
    private static Study createdStudy;

    /**
     * A synthetic user-id that represents the "new member" we try to add.
     * In a real environment this would be a real Shanoir user-id; here we use
     * a fixed value that is guaranteed not to collide with the expert's own id.
     */
    private static final Long NEW_MEMBER_USER_ID = 99_999L;

    @BeforeEach
    void requireBothClients() {
        requireAdminClient();
        requireExpertClient();
    }

    /**
     * An expert creates a brand-new study. According to
     * {@code StudyApiController#saveNewStudy}, the {@code isDraft} flag is forced
     * to {@code true} for any non-admin caller, so the study must arrive in draft
     * state after creation.
     */
    @Test
    @Order(1)
    void expertCreatesStudyDraft() {
        LOG.info("Step 1 - Expert creates a study expected in DRAFT state.");
        Study study = buildMinimalStudy();
        createdStudy = expertClient.createStudy(study);
        Assertions.assertNotNull(createdStudy,
                "Study creation by expert must not return null.");
        Assertions.assertNotNull(createdStudy.getId(),
                "Created study must have a server-assigned id.");
        Assertions.assertTrue(Boolean.TRUE.equals(createdStudy.getIsDraft()),
                "Study created by an expert must be in DRAFT state (isDraft=true).");
        LOG.info("Step 1 passed - study id={} is in DRAFT.", createdStudy.getId());
    }

    /**
     * Adding a member to a draft study must be rejected by the server.
     * The expected behaviour is either a {@code null} return value or an
     * exception thrown by the client, depending on how
     * {@link ShanoirUploaderServiceClient#addStudyUser} surfaces HTTP error
     * codes.  Either outcome satisfies the assertion.
     */
    @Test
    @Order(2)
    void expertAddsUserToDraftStudy_fails() {
        Assertions.assertNotNull(createdStudy,
                "Step 2 requires Step 1 to have run successfully.");
        LOG.info("Step 2 - Expert tries to add user {} to DRAFT study {}.",
                NEW_MEMBER_USER_ID, createdStudy.getId());
        StudyUser candidate = buildStudyUser(createdStudy.getId(), NEW_MEMBER_USER_ID);
        boolean addFailed = false;
        try {
            StudyUser result = expertClient.addStudyUser(createdStudy.getId(), candidate);
            // A null result means the server rejected the request
            if (result == null) {
                addFailed = true;
            }
        } catch (Exception e) {
            // Any exception (e.g. HTTP 422 / 403 surfaced as RuntimeException)
            // also counts as the expected failure
            LOG.info("Step 2 – addStudyUser threw (expected): {}", e.getMessage());
            addFailed = true;
        }
        Assertions.assertTrue(addFailed,
                "Adding a user to a DRAFT study must fail before approval.");
        LOG.info("Step 2 passed - attempt to add member to DRAFT study was rejected.");
    }

    /**
     * Only a user with {@code ROLE_ADMIN} may call
     * {@code /studies/approveDraftStudy/{studyId}}.  After approval the returned
     * study must have {@code isDraft=false}.
     */
    @Test
    @Order(3)
    void adminApprovesDraftStudy_becomesNonDraft() {
        Assertions.assertNotNull(createdStudy,
                "Step 3 requires Step 1 to have run successfully.");
        LOG.info("Step 3 - Admin approves study {}.", createdStudy.getId());
        Study approvedStudy = adminClient.approveDraftStudy(createdStudy.getId());
        Assertions.assertNotNull(approvedStudy,
                "approveDraftStudy must return the updated study.");
        Assertions.assertFalse(Boolean.TRUE.equals(approvedStudy.getIsDraft()),
                "Study must no longer be in DRAFT state after admin approval.");
        // Keep the local reference up-to-date so subsequent steps see the right state
        createdStudy = approvedStudy;
        LOG.info("Step 3 passed - study id={} is now approved (isDraft={}).",
                createdStudy.getId(), createdStudy.getIsDraft());
    }

    /**
     * After approval the same add-member request that failed in step 2 must now
     * succeed and return a non-null {@link StudyUser} with the expected user-id.
     */
    @Test
    @Order(4)
    void expertAddsUserToApprovedStudy_succeeds() {
        Assertions.assertNotNull(createdStudy,
                "Step 4 requires Steps 1-3 to have run successfully.");
        Assertions.assertFalse(Boolean.TRUE.equals(createdStudy.getIsDraft()),
                "Step 4 requires the study to have been approved in Step 3.");
        LOG.info("Step 4 - Expert adds user {} to approved study {}.",
                NEW_MEMBER_USER_ID, createdStudy.getId());
        StudyUser candidate = buildStudyUser(createdStudy.getId(), NEW_MEMBER_USER_ID);
        StudyUser created = null;
        try {
            created = expertClient.addStudyUser(createdStudy.getId(), candidate);
        } catch (Exception e) {
            Assertions.fail("addStudyUser must not throw after study approval, but got: " + e.getMessage());
        }
        Assertions.assertNotNull(created,
                "addStudyUser must return a non-null StudyUser after approval.");
        Assertions.assertEquals(NEW_MEMBER_USER_ID, created.getUserId(),
                "The returned StudyUser must carry the id of the newly added user.");
        LOG.info("Step 4 passed - user {} successfully added to study {}.",
                NEW_MEMBER_USER_ID, createdStudy.getId());
    }

    /**
     * Builds a minimal valid {@link Study} payload suitable for a POST to the
     * studies endpoint.  Mirrors the structure used in
     * {@link AbstractTest#createStudyAndCenterAndStudyCard()} but deliberately
     * omits the study-card (not needed for the approval flow under test).
     */
    private Study buildMinimalStudy() {
        StudyExtraDetails extraDetails = new StudyExtraDetails();
        extraDetails.setExpectedNbOfSubjects(5L);
        extraDetails.setExpectedNbOfCenters(1L);
        extraDetails.setSponsor("Test-Sponsor");
        extraDetails.setPrincipalInvestigator("Test-Principal-Investigator");

        Study study = new Study();
        study.setExtraDetails(extraDetails);
        study.setName("Study-Draft-Approval-" + UUID.randomUUID());
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

    /**
     * Builds a {@link StudyUser} candidate for the given study and user ids with
     * a standard set of import/view rights.
     *
     * @param studyId the target study
     * @param userId  the user to add
     * @return a populated but not-yet-persisted {@link StudyUser}
     */
    private StudyUser buildStudyUser(Long studyId, Long userId) {
        StudyUser su = new StudyUser();
        su.setStudyId(studyId);
        su.setUserId(userId);
        su.setStudyUserRights(Arrays.asList(
                StudyUserRight.CAN_SEE_ALL,
                StudyUserRight.CAN_DOWNLOAD,
                StudyUserRight.CAN_IMPORT));
        return su;
    }

}