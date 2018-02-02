package org.shanoir.ng.importer.strategies.datasetacquisition;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class DatasetAcquisitionFactory {
	
	private static Map<String,DatasetAcquisitionStrategy> instances;
	
	static {
		instances = new HashMap<>();
		instances.put("MR", new MrDatasetAcquisitionStrategy());
	}

	public static <T extends DatasetAcquisitionStrategy> T getDatasetAcquisitionStrategy(String type) {
		return (T) instances.get(type);
	}
}
