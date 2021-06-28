package org.shanoir.ng.dataset.service;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.shared.core.service.BasicEntityServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DatasetExpressionServiceImpl extends BasicEntityServiceImpl<DatasetExpression> implements DatasetExpressionService {

	@Override
	protected DatasetExpression updateValues(DatasetExpression from, DatasetExpression to) {
		to.setComingFromDatasetExpressions(from.getComingFromDatasetExpressions());
		to.setDataset(from.getDataset());
		to.setDatasetExpressionFormat(from.getDatasetExpressionFormat());
		to.setDatasetFiles(from.getDatasetFiles());
		to.setDatasetProcessingType(from.getDatasetProcessingType());
		to.setFirstImageAcquisitionTime(from.getFirstImageAcquisitionTime());
		to.setFrameCount(from.getFrameCount());
		to.setLastImageAcquisitionTime(from.getLastImageAcquisitionTime());
		to.setMultiFrame(from.isMultiFrame());
		to.setNiftiConverterId(from.getNiftiConverterId());
		to.setNiftiConverterVersion(from.getNiftiConverterVersion());
		to.setOriginalDatasetExpression(from.getOriginalDatasetExpression());
		to.setOriginalNiftiConversion(from.getOriginalNiftiConversion());
		return to;
	}

}
