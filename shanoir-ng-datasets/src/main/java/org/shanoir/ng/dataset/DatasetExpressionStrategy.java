package org.shanoir.ng.dataset;

import org.shanoir.ng.dataset.modality.MrDataset;
import org.shanoir.ng.importer.dto.Dataset;
import org.shanoir.ng.importer.dto.ExpressionFormat;
import org.shanoir.ng.importer.dto.ImportJob;
import org.shanoir.ng.importer.dto.Serie;

public interface DatasetExpressionStrategy {
	
	DatasetExpression generateDatasetExpression(Serie serie, Dataset dataset, MrDataset mrDataset,ImportJob importJob,ExpressionFormat expressionFormat);

}
