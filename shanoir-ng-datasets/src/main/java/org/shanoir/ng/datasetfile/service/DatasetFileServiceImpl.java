package org.shanoir.ng.datasetfile.service;

import org.shanoir.ng.datasetfile.DatasetFile;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DatasetFileServiceImpl extends BasicEntityServiceImpl<DatasetFile> implements DatasetFileService {

	@Override
	protected DatasetFile updateValues(DatasetFile from, DatasetFile to) {
		to.setDatasetExpression(from.getDatasetExpression());
		to.setPacs(from.isPacs());
		to.setPath(from.getPath());
		return to;
	}

}
