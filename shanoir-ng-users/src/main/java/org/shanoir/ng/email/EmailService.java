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

package org.shanoir.ng.email;

import java.util.List;

import org.shanoir.ng.accessrequest.model.AccessRequest;
import org.shanoir.ng.shared.email.DuaDraftWrapper;
import org.shanoir.ng.shared.email.EmailDatasetImportFailed;
import org.shanoir.ng.shared.email.EmailDatasetsImported;
import org.shanoir.ng.shared.email.EmailStudyUsersAdded;
import org.shanoir.ng.shared.email.StudyInvitationEmail;
import org.shanoir.ng.shared.email.EmailStudy;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.user.model.User;

/**
 * Email service.
 *
 * @author msimon, mkain
 *
 */
public interface EmailService {

    /**
     * Send an email to user if account will expire soon.
     *
     * @param user
     *            user.
     */
    void notifyAccountWillExpire(User user);

    /**
     * Send an email to administrators to indicate an account extension request.
     *
     * @param user
     *            updated user.
     */
    void notifyAdminAccountExtensionRequest(User user);

    /**
     * Send an email to the user and all administrators on account request validation.
     *
     * @param user
     *            accepted user.
     */
    void notifyAccountRequestAccepted(User user);

    /**
     * Send an email to the user and all administrators on account request validation.
     *
     * @param user
     *            accepted user.
     */
    void notifyAccountRequestDenied(User user);

    /**
     * Send an email to the user and all administrators on extension request validation.
     *
     * @param user
     *            user.
     */
    void notifyExtensionRequestAccepted(User user);

    /**
     * Send an email to the user and all administrators on extension request deny.
     *
     * @param user
     *            user.
     */
    void notifyExtensionRequestDenied(User user);

    /**
     * Send an email on account creation.
     *
     * @param user
     *            created user.
     * @param password
     *            user password.
     */
    void notifyCreateUser(User user, String password);

    /**
     * Send an email on account creation.
     *
     * @param user
     *            created user.
     * @param password
     *            user password.
     */
    void notifyCreateAccountRequest(User user, String password);

    /**
     * Send an email on user password reset.
     *
     * @param user
     *            user.
     * @param password
     *            new password.
     */
    void notifyUserResetPassword(User user, String password);

    /**
     *  This method notifies a study manager that some data was imported in the study.
     * @param generatedMail: The object containing all the mail informations
     */
    void notifyStudyManagerDataImported(EmailDatasetsImported generatedMail);

    /**
     * This method notifies a study manager that one ore more new members, StudyUsers,
     * have been added to his study.
     *
     * @param mail
     */
    void notifyStudyManagerStudyUsersAdded(EmailStudyUsersAdded email);

    void notifyAdminStudyEvent(EmailStudy email);

    void notifyStudyMembersStudyApproval(EmailStudy email);

    /**
     *  This method notifies a study manager that an import failed for a given study
     * @param generatedMail: The object containing all the mail informations
     */
    void notifyStudyManagerImportFailure(EmailDatasetImportFailed generatedMail);

    /** Invites an user to join a study. */
    void inviteToStudy(StudyInvitationEmail mail);

    /**
     * This method notifies a study manager that a user asked an access to a study.
     * @param createdRequest the access request object
     * @throws ShanoirException
     */
    void notifyStudyManagerAccessRequest(AccessRequest createdRequest);

    /**
     * Notify a user its study request was refused
     * @param accessRequest the access request
     */
    void notifyUserRefusedFromStudy(AccessRequest refusedRequest);

    public void notifyDuaDraftCreation(DuaDraftWrapper mail);

    /**
     * Send an announcement to each of the given users as an individual email.
     * A failure on one recipient is logged and does not prevent sending to the
     * others.
     *
     * @param recipients
     *            the users to email.
     * @param subject
     *            the email subject, prefixed with the study name for a study
     *            email.
     * @param content
     *            the plain text announcement, rendered with its line breaks.
     * @param sender
     *            the user sending the email, named in the signature and used as
     *            reply address of a study email. May be null.
     * @param studyName
     *            the name of the study the members of which are emailed, null
     *            for a platform wide announcement signed by the administrator.
     */
    void sendMassEmail(List<User> recipients, String subject, String content, User sender, String studyName);

}
