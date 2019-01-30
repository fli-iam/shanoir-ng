package org.shanoir.ng.utils;

import java.util.ArrayList;

import org.shanoir.ng.acquisitionequipment.AcquisitionEquipment;
import org.shanoir.ng.center.Center;
import org.shanoir.ng.coil.Coil;
import org.shanoir.ng.coil.CoilType;
import org.shanoir.ng.manufacturermodel.DatasetModalityType;
import org.shanoir.ng.manufacturermodel.Manufacturer;
import org.shanoir.ng.manufacturermodel.ManufacturerModel;
import org.shanoir.ng.shared.common.CommonIdsDTO;
import org.shanoir.ng.study.Study;
import org.shanoir.ng.studyuser.StudyUser;
import org.shanoir.ng.studyuser.StudyUserRight;
import org.shanoir.ng.subject.Subject;

/**
 * Utility class for test. Generates models.
 * 
 * @author msimon
 *
 */
public final class ModelsUtil {

	// Acquisition equipment data
	public static final String ACQ_EQPT_SERIAL_NUMBER = "123456789";

	// Center data
	public static final String CENTER_NAME = "center";

	// Coil data
	public static final String COIL_NAME = "coil";

	// Manufacturer data
	public static final String MANUFACTURER_NAME = "manufacturer";

	// Manufacturer model data
	public static final String MANUFACTURER_MODEL_NAME = "manufacturerModel";

	// Study data
	public static final Long STUDY_ID = 1L;
	public static final String STUDY_NAME = "study";

	// Subject data
	public static final String SUBJECT_NAME = "subject";

	// User data
	public static final Long USER_ID = 1L;
	
	/**
	 * Create a center.
	 * 
	 * @return center.
	 */
	public static Center createCenter() {
		final Center center = new Center();
		center.setAcquisitionEquipments(new ArrayList<>());
		center.setName(CENTER_NAME);
		center.setStudyCenterList(new ArrayList<>());
		return center;
	}

	/**
	 * Create a coil.
	 * 
	 * @return coil.
	 */
	public static Coil createCoil() {
		final Coil coil = new Coil();
		coil.setCenter(createCenter());
		coil.setCoilType(CoilType.BODY);
		coil.setManufacturerModel(createManufacturerModel());
		coil.setName(COIL_NAME);
		return coil;
	}

	/**
	 * Create an acquisition equipment.
	 * 
	 * @return acquisition equipment.
	 */
	public static AcquisitionEquipment createAcquisitionEquipment() {
		final AcquisitionEquipment equipment = new AcquisitionEquipment();
		equipment.setCenter(createCenter());
		equipment.setManufacturerModel(createManufacturerModel());
		equipment.setSerialNumber(ACQ_EQPT_SERIAL_NUMBER);
		return equipment;
	}


	/**
	 * Create a DTO with ids.
	 * 
	 * @return DTO with ids.
	 */
	public static CommonIdsDTO createCommonIdsDTO() {
		final CommonIdsDTO commonIdsDTO = new CommonIdsDTO();
		commonIdsDTO.setCenterId(1L);
		commonIdsDTO.setStudyId(1L);
		commonIdsDTO.setSubjectId(1L);
		return commonIdsDTO;
	}

	/**
	 * Create a manufacturer model.
	 * 
	 * @return manufacturer model.
	 */
	public static ManufacturerModel createManufacturerModel() {
		final ManufacturerModel manufacturerModel = new ManufacturerModel();
		manufacturerModel.setDatasetModalityType(DatasetModalityType.MR_DATASET);
		manufacturerModel.setMagneticField(3D);
		final Manufacturer manufacturer = createManufacturer();
		manufacturer.setId(1L);
		manufacturerModel.setManufacturer(manufacturer);
		manufacturerModel.setName(MANUFACTURER_MODEL_NAME);
		return manufacturerModel;
	}

	/**
	 * Create a manufacturer.
	 * 
	 * @return manufacturer.
	 */
	public static Manufacturer createManufacturer() {
		final Manufacturer manufacturer = new Manufacturer();
		manufacturer.setName(MANUFACTURER_NAME);
		return manufacturer;
	}

	/**
	 * Create a study.
	 * 
	 * @return study.
	 */
	public static Study createStudy() {
		final Study study = new Study();
		study.setName(STUDY_NAME);
		study.setStudyCenterList(new ArrayList<>());
		study.setStudyUserList(new ArrayList<>());
		return study;
	}

	/**
	 * Create a relation between a study and a user.
	 * 
	 * @return relation.
	 */
	public static StudyUser createStudyUser() {
		final StudyUser studyUser = new StudyUser();
		studyUser.setStudyId(STUDY_ID);
		studyUser.setStudyUserType(StudyUserRight.RESPONSIBLE);
		studyUser.setUserId(USER_ID);
		return studyUser;
	}

	public static Subject createSubject() {
		final Subject subject = new Subject();
		subject.setName(SUBJECT_NAME);
		/*
		 * subject.setBirthDate(null); subject.setIdentifier(null);
		 * subject.setPseudonymusHashValues(null); subject.setSex(null);
		 * subject.setLinks(null); subject.setSubjectStudyList(null);
		 */
		return subject;
	}

}
