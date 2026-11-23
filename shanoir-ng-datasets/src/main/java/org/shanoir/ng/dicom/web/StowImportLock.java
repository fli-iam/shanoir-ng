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


package org.shanoir.ng.dicom.web;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Component;

/**
 * Lets one STOW-RS instance of a subject be imported at a time.
 *
 * The importer looks up the subject, the examination, the acquisition and the
 * dataset of an instance and creates what is missing, each request in its own
 * transaction. Two requests of the same subject that run together both see
 * nothing yet and both create it: a duplicate subject key, or two acquisitions
 * for one series, after which every further instance of that series fails.
 * Everything that is created is scoped to the subject (the subject itself is
 * found by name and study), so the lock is keyed on the study and the patient
 * name, not on the examination: one patient with several examinations in flight
 * would otherwise still race on the subject.
 *
 * The lock is taken by the request filter around the call to the importer, so it
 * is released only after the importer's transaction has committed. Requests of
 * different subjects still run in parallel. The stripes are shared by subjects
 * whose keys hash alike, which only costs them some parallelism.
 *
 * It protects a single instance of the service, which is how the datasets
 * microservice is deployed.
 */
@Component
public class StowImportLock {

    private static final int STRIPES = 64;

    private final Lock[] locks = new Lock[STRIPES];

    public StowImportLock() {
        for (int i = 0; i < STRIPES; i++) {
            locks[i] = new ReentrantLock();
        }
    }

    /**
     * @param studyId the ClinicalTrialProtocolID of the instance, the Shanoir study, may be null
     * @param patientName the PatientName of the instance, the Shanoir subject name, may be null
     * @return the lock to hold while importing an instance of that subject
     */
    public Lock lockFor(String studyId, String patientName) {
        int hash = Objects.hash(studyId, patientName);
        return locks[Math.floorMod(hash, STRIPES)];
    }

}
