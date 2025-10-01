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