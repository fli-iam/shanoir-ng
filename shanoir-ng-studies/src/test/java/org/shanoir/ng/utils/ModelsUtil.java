/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.utils;

import java.util.ArrayList;
import java.util.Arrays;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.coil.model.Coil;
import org.shanoir.ng.coil.model.CoilType;
import org.shanoir.ng.manufacturermodel.model.DatasetModalityType;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.security.rights.StudyUserRight;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.model.StudyUser;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;

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
		StudyCenter sc = new StudyCenter();
		sc.setStudy(study); sc.setCenter(createCenter()); sc.setId(1L);
		study.getStudyCenterList().add(sc);
		study.setStudyUserList(new ArrayList<>());
		study.setTags(new ArrayList<>());
		return study;
	}

	/**
	 * Create a relation between a study and a user.
	 * 
	 * @return relation.
	 */
	public static StudyUser createStudyUser() {
		final StudyUser studyUser = new StudyUser();
		studyUser.setStudy(createStudy());
		studyUser.setStudyUserRights(Arrays.asList(StudyUserRight.CAN_ADMINISTRATE));
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
