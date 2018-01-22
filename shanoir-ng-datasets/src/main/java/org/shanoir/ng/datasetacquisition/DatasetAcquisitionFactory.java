package org.shanoir.ng.datasetacquisition;

import java.util.HashMap;
import java.util.Map;

import org.shanoir.ng.datasetacquisition.mr.MrDatasetAcquisitionStrategy;
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
