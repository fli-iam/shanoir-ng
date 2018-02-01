package org.shanoir.ng.importer.strategies.datasetexpression;


import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DatasetExpressionFactory {
	
	private static Map<String,DatasetExpressionStrategy> instances;
	
	static {
		instances = new HashMap<>();
		instances.put("dcm", new DicomDatasetExpressionStrategy());
		instances.put("nifti", new NiftiDatasetExpressionStrategy());
	}

	public static <T extends DatasetExpressionStrategy> T getDatasetExpressionStrategy(String type) {
		return (T) instances.get(type);
	}
}