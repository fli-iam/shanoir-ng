package org.shanoir.ng.datasetacquisition;

import org.shanoir.ng.dataset.DatasetApiController;
import org.shanoir.ng.dicom.DicomProcessing;
import org.shanoir.ng.examination.Examination;
import org.shanoir.ng.examination.ExaminationService;
import org.shanoir.ng.importer.dto.Serie;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * dataset acquisition service implementation.
 * 
 * @author atouboul
 *
 */

@Service
public class DatasetAcquisitionServiceImpl implements DatasetAcquisitionService<DatasetAcquisition> {

	private static final Logger LOG = LoggerFactory.getLogger(DatasetAcquisitionServiceImpl.class);
	
	@Autowired
	DicomProcessing dicomProcessing;
	
	@Autowired
	ExaminationService examinationService;

	@Override
	public void createDatasetAcquisition(Serie serie, int rank, Examination examination) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteById(Long id) throws ShanoirException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatasetAcquisition findById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatasetAcquisition save(DatasetAcquisition DatasetAcquisition) throws ShanoirException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatasetAcquisition update(DatasetAcquisition dataset) throws ShanoirException {
		// TODO Auto-generated method stub
		return null;
	}

}
