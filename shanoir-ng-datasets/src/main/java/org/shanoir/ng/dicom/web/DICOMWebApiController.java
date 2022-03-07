package org.shanoir.ng.dicom.web;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;

public class DICOMWebApiController implements DICOMWebApi {

	@Override
	public ResponseEntity<String> findPatients() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findStudies() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findSeries() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findSeriesOfStudy(String studyInstanceUID) throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findInstances() throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findInstancesOfStudy(String studyInstanceUID) throws RestServiceException {
		return null;
	}

	@Override
	public ResponseEntity<String> findInstancesOfStudyOfSerie(String studyInstanceUID, String serieInstanceUID)
			throws RestServiceException {
		return null;
	}

}
