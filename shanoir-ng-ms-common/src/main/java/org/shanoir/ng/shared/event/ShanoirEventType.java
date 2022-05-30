package org.shanoir.ng.shared.event;

public class ShanoirEventType {

	/** Study **/
	public static final String CREATE_STUDY_EVENT = "createStudy.event";
	public static final String UPDATE_STUDY_EVENT = "updateStudy.event";
	public static final String DELETE_STUDY_EVENT = "deleteStudy.event";

	/** Center **/
	public static final String CREATE_CENTER_EVENT = "createCenter.event";
	public static final String UPDATE_CENTER_EVENT = "updateCenter.event";
	public static final String DELETE_CENTER_EVENT = "deleteCenter.event";

	/** Subject**/
	public static final String CREATE_SUBJECT_EVENT = "createSubject.event";
	public static final String UPDATE_SUBJECT_EVENT = "updateSubject.event";
	public static final String DELETE_SUBJECT_EVENT = "deleteSubject.event";

	/** Examination **/
	public static final String CREATE_EXAMINATION_EVENT = "createExamination.event";
	public static final String UPDATE_EXAMINATION_EVENT = "updateExamination.event";
	public static final String DELETE_EXAMINATION_EVENT = "deleteExamination.event";

	/** Dataset **/
	public static final String CREATE_DATASET_EVENT = "createDataset.event";
	public static final String UPDATE_DATASET_EVENT = "updateDataset.event";
	public static final String DELETE_DATASET_EVENT = "deleteDataset.event";

	/** Dataset **/
	public static final String CREATE_DATASET_ACQUISITION_EVENT = "createDatasetAcquisition.event";
	public static final String UPDATE_DATASET_ACQUISITION_EVENT = "updateDatasetAcquisition.event";
	public static final String DELETE_DATASET_ACQUISITION_EVENT = "deleteDatasetAcquisition.event";

	/** Equipement **/
	public static final String CREATE_EQUIPEMENT_EVENT = "createEquipement.event";
	public static final String UPDATE_EQUIPEMENT_EVENT = "updateEquipement.event";
	public static final String DELETE_EQUIPEMENT_EVENT = "deleteEquipement.event";

	/** Coil **/
	public static final String CREATE_COIL_EVENT = "createCoil.event";
	public static final String UPDATE_COIL_EVENT = "updateCoil.event";
	public static final String DELETE_COIL_EVENT = "deleteCoil.event";

	/** Pathology **/
	public static final String CREATE_PATHOLOGY_EVENT = "createPathology.event";
	public static final String UPDATE_PATHOLOGY_EVENT = "updatePathology.event";
	public static final String DELETE_PATHOLOGY_EVENT = "deletePathology.event";

	/** Therapy **/
	public static final String CREATE_THERAPY_EVENT = "createTherapy.event";
	public static final String UPDATE_THERAPY_EVENT = "updateTherapy.event";
	public static final String DELETE_THERAPY_EVENT = "deleteTherapy.event";

	/** Anesthetic **/
	public static final String CREATE_ANESTHETIC_EVENT = "createAnesthetic.event";
	public static final String UPDATE_ANESTHETIC_EVENT = "updateAnesthetic.event";
	public static final String DELETE_ANESTHETIC_EVENT = "deleteAnesthetic.event";

	/** Preclinical Subject**/
	public static final String CREATE_PRECLINICAL_SUBJECT_EVENT = "createPreclinicalSubject.event";
	public static final String UPDATE_PRECLINICAL_SUBJECT_EVENT = "updatePreclinicalSubject.event";
	public static final String DELETE_PRECLINICAL_SUBJECT_EVENT = "deletePreclinicalSubject.event";

	/** preclinical Reference **/
	public static final String CREATE_PRECLINICAL_REFERENCE_EVENT = "createPreclinicalReference.event";
	public static final String UPDATE_PRECLINICAL_REFERENCE_EVENT = "updatePreclinicalReference.event";
	public static final String DELETE_PRECLINICAL_REFERENCE_EVENT = "deletePreclinicalReference.event";
	
	/** User **/
	public static final String UPDATE_USER_EVENT = "updateUser.event";
	public static final String DELETE_USER_EVENT = "deleteUser.event";
	
	/** Import Dataset **/
	public static final String IMPORT_DATASET_EVENT = "importDataset.event";

	/** Download dataset. */
	public static final String DOWNLOAD_DATASET_EVENT = "downloadDataset.event";

	/** User subscribed to a challenge. */
	public static final String CHALLENGE_SUBSCRIPTION_EVENT = "challengeSubscription.event";
}
