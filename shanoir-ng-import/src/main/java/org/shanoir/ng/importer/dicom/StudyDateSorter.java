package org.shanoir.ng.importer.dicom;

import java.time.LocalDate;
import java.util.Comparator;

import org.shanoir.ng.importer.model.Study;

/**
 * Sorts a list of DICOM studies according to their studyDate.
 *
 * @author mkain
 *
 */
public class StudyDateSorter implements Comparator<Study> {

	@Override
	public int compare(Study s1, Study s2) {
		LocalDate s1StudyDate = s1.getStudyDate();
		LocalDate s2StudyDate = s2.getStudyDate();
        if (s1StudyDate.isEqual(s2StudyDate)) {
            return 0;
        } else {
            if (s1StudyDate.isBefore(s2StudyDate)) {
                return -1;
            } else {
                return 1;
            }
        }
	}

}