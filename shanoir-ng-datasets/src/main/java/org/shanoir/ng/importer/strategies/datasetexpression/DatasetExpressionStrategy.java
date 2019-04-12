package org.shanoir.ng.importer.strategies.datasetexpression;

import org.shanoir.ng.dataset.model.DatasetExpression;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;

public interface DatasetExpressionStrategy {
	
	DatasetExpression generateDatasetExpression(Serie serie, ImportJob importJob,ExpressionFormat expressionFormat);

}
