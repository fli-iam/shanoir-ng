package org.shanoir.ng.importer.strategies.datasetexpression;


import org.shanoir.ng.dataset.DatasetExpression;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * In the context of the strategy pattern this class represents the context.
 * The context holds the variable to the actual strategy in use, that has
 * been chosen on using the defined format.
 * 
 * @author mkain
 *
 */
@Service
public class DatasetExpressionContext implements DatasetExpressionStrategy {
	
	@Autowired
	private DicomDatasetExpressionStrategy dicomDatasetExpressionStrategy;

	@Autowired
	private NiftiDatasetExpressionStrategy niftiDatasetExpressionStrategy;
	
	private DatasetExpressionStrategy datasetExpressionStrategy;

	/**
	 * 1) Call first with the given modality to choose the right strategy.
	 * @param modality
	 */
	public void setDatasetExpressionStrategy(String format) {
		if ("dcm".equals(format)) {
			this.datasetExpressionStrategy = dicomDatasetExpressionStrategy;
		} else if ("nii".equals(format)) {
			this.datasetExpressionStrategy = niftiDatasetExpressionStrategy;
		}
		// else... add other format strategies here
	}

	@Override
	public DatasetExpression generateDatasetExpression(Serie serie, ImportJob importJob,
			ExpressionFormat expressionFormat) {
		if (datasetExpressionStrategy != null) {
			return datasetExpressionStrategy.generateDatasetExpression(serie, importJob, expressionFormat);
		}
		return null;
	}

}