package org.shanoir.ng.dataset.modality;

/**
 * This enumeration contains all 24 possible values for value 3
 * of image type (0008,0008) and frame type (0008,9007) tags/
 * attributes in dicom enhanced "Enhanced MR Image", so only for MR.
 * This value 3 is called image flavor. See more information according to:
 * https://www.dicomstandard.org/News-dir/ftsup/docs/sups/sup49.pdf
 * https://dicom.innolitics.com/ciods/enhanced-mr-image/enhanced-mr-image/00080008
 *
 * @author mkain
 *
 */
public enum ImageFlavor {

	ANGIO_TIME,
	
	METABOLITE_MAP,
	
	CINE,
	
	DIFFUSION,
	
	FLOW_ENCODED,
	
	FLUID_ATTENUATED,
	
	FMRI,
	
	LOCALIZER,
	
	MAX_IP,
	
	MIN_IP,
	
	M_MODE,
	
	MOTION,
	
	PERFUSION,
	
	PROTON_DENSITY,
	
	REALTIME,
	
	STIR,
	
	STRESS,
	
	TAGGING,
	
	TEMPERATURE,
	
	T1,
	
	T2,
	
	T2_STAR,
	
	TOF,
	
	VELOCITY

}
