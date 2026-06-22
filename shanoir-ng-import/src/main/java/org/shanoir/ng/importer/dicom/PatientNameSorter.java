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

package org.shanoir.ng.importer.dicom;

import java.util.Comparator;

import org.shanoir.ng.importer.model.Patient;

/**
 * Sorts a list of DICOM patients according to their name.
 *
 * @author mkain
 *
 */
public class PatientNameSorter implements Comparator<Patient> {

    @Override
    public int compare(Patient p1, Patient p2) {
        String patientName1 = p1.getPatientName();
        String patientName2 = p2.getPatientName();
        return patientName1.compareTo(patientName2);
    }

}
