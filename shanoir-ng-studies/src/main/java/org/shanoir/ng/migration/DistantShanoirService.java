package org.shanoir.ng.migration;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.subject.model.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DistantShanoirService {
	
	private static final String SERVICE_SUBJECTS_CREATE = "ToBeDefined";
	
	private static final String SERVICE_STUDY_CREATE = "study/studies";
	
	private static final String ADD_PROTOCOL_FILE_PATH = "ToBeDefined";

	private static final String GET_CENTERS = "ToBeDefined";

	private static final String CREATE_CENTER = "ToBeDefined";
	
	private static final String GET_EQUIPEMENTS = "ToBeDefined";
	
	private static final String GET_MODELS = "ToBeDefined";

	private static final String GET_MANUFACTURERS = "ToBeDefined";
	
	@Autowired
	RestTemplate restTemplate;

	@Autowired
	StudyService studyService;

	/**
	 * Creates a subject in the distant shanoir instance
	 * @param subject the subject to create
	 */
	public Subject createSubject(Subject subject) {
		try {
			ResponseEntity<Subject> response = this.restTemplate.exchange(new URI(SERVICE_SUBJECTS_CREATE), HttpMethod.POST, new HttpEntity<>(subject), Subject.class);
			// Check that it's OK
			return response.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates a study in the distant shanoir instance
	 * @param study the study to create
	 */
	public Study createStudy(Study study) {
		try {
			ResponseEntity<Study> response = this.restTemplate.exchange(new URI(SERVICE_STUDY_CREATE), HttpMethod.POST, new HttpEntity<>(study), Study.class);
			// Check that it's OK
			return response.getBody();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates a study in the distant shanoir instance
	 * @param studyId the old study ID where to create the new file
	 * @param file the file to send
	 */
	public void addProtocoleFile(File file) {
		try {
			ResponseEntity<Study> response = this.restTemplate.exchange(new URI(ADD_PROTOCOL_FILE_PATH), HttpMethod.POST, new HttpEntity<>(file), Study.class);
			// Check that it's OK
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the IdName list of all centers.
	 */
	public List<IdName> getAllCenters() {
		try {
			ResponseEntity<IdName> response = this.restTemplate.exchange(new URI(GET_CENTERS), HttpMethod.GET, null, IdName.class);
			// Check that it's OK
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Create a new center
	 * @param center
	 */
	public Center createCenter(Center center) {
		try {
			ResponseEntity<Center> response = this.restTemplate.exchange(new URI(CREATE_CENTER), HttpMethod.POST, new HttpEntity<Center>(center), Center.class);
			// Check that it's OK
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<AcquisitionEquipment> getAcquisitionEquipements() {
		try {
			ResponseEntity<AcquisitionEquipment> response = this.restTemplate.exchange(new URI(GET_EQUIPEMENTS), HttpMethod.GET, null, AcquisitionEquipment.class);
			// Check that it's OK
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<ManufacturerModel> getModels() {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Manufacturer> getManufacturers() {
		// TODO Auto-generated method stub
		return null;
	}

	public AcquisitionEquipment createEquipement(AcquisitionEquipment equipement) {
		return null;
	}

	public Manufacturer createManufacturer(Manufacturer manufacturer) {
		// TODO Auto-generated method stub
		return null;
		
	}

	public ManufacturerModel createManufacturerModel(ManufacturerModel manufacturerModel) {
		// TODO Auto-generated method stub
		return null;
	}
}
