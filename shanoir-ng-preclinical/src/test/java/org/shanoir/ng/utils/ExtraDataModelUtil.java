package org.shanoir.ng.utils;

import org.shanoir.ng.preclinical.extra_data.bloodgas_data.BloodGasData;
import org.shanoir.ng.preclinical.extra_data.examination_extra_data.ExaminationExtraData;
import org.shanoir.ng.preclinical.extra_data.physiological_data.PhysiologicalData;

/**
 * Utility class for test.
 * Generates extra data 
 * 
 * @author sloury
 *
 */
public final class ExtraDataModelUtil {

	
	public static final Long EXAMINATION_ID = 1L;
	public static final Long EXTRADATA_ID = 1L;
	public static final String EXTRADATA_TYPE = "Extra data";
	public static final Long PHYSIOLOGICALDATA_ID = 2L;
	public static final String PHYSIOLOGICALDATA_TYPE = "Physiological data";
	public static final Long BLOODGASDATA_ID = 3L;
	public static final String BLOODGASDATA_TYPE = "Blood gas data";
	public static final String EXTRADATA_FILEPATH = "/home/sloury/Documents/FLI-IAM/SHANOIR_NG/upload/";
	public static final String EXTRADATA_FILENAME = "extradata.txt";
	
	/**
	 * Create an extrdata.
	 * 
	 * @return extrdata.
	 */
	public static ExaminationExtraData createExaminationExtraData() {
		ExaminationExtraData extradata = new ExaminationExtraData();
		extradata.setId(EXTRADATA_ID);
		extradata.setExaminationId(EXAMINATION_ID);
		extradata.setExtradatatype(EXTRADATA_TYPE);
		extradata.setFilename(EXTRADATA_FILENAME);
		extradata.setFilepath(EXTRADATA_FILEPATH);
		return extradata;
	}
	public static PhysiologicalData createExaminationPhysiologicalData() {
		PhysiologicalData extradata = new PhysiologicalData();
		extradata.setId(PHYSIOLOGICALDATA_ID);
		extradata.setExaminationId(EXAMINATION_ID);
		extradata.setExtradatatype(PHYSIOLOGICALDATA_TYPE);
		extradata.setFilename(EXTRADATA_FILENAME);
		extradata.setFilepath(EXTRADATA_FILEPATH);
		extradata.setHas_heart_rate(true);
		extradata.setHas_respiratory_rate(true);
		extradata.setHas_sao2(false);
		extradata.setHas_temperature(false);
		return extradata;
	}
	public static BloodGasData createExaminationBloodGasData() {
		BloodGasData extradata = new BloodGasData();
		extradata.setId(BLOODGASDATA_ID);
		extradata.setExaminationId(EXAMINATION_ID);
		extradata.setExtradatatype(BLOODGASDATA_TYPE);
		extradata.setFilename(EXTRADATA_FILENAME);
		extradata.setFilepath(EXTRADATA_FILEPATH);
		return extradata;
	}
		
}
