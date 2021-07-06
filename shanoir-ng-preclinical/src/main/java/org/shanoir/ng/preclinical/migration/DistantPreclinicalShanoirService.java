package org.shanoir.ng.preclinical.migration;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.shanoir.ng.preclinical.anesthetics.anesthetic.Anesthetic;
import org.shanoir.ng.preclinical.anesthetics.examination_anesthetics.ExaminationAnesthetic;
import org.shanoir.ng.preclinical.anesthetics.ingredients.AnestheticIngredient;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.pathology_models.PathologyModel;
import org.shanoir.ng.preclinical.pathologies.subject_pathologies.SubjectPathology;
import org.shanoir.ng.preclinical.references.Reference;
import org.shanoir.ng.preclinical.subjects.AnimalSubject;
import org.shanoir.ng.preclinical.therapies.Therapy;
import org.shanoir.ng.preclinical.therapies.subject_therapies.SubjectTherapy;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class DistantPreclinicalShanoirService {

	private static final Logger LOG = LoggerFactory.getLogger(DistantPreclinicalShanoirService.class);


	@Autowired
	DistantKeycloakConfigurationService distantKeycloak;

	private static final String GET_REFERENCES = "/shanoir-ng/preclinical/refs/";

	private static final String GET_ANESTHETICS = "/shanoir-ng/preclinical/anesthetic/";

	private static final String GET_PATHOLOGY_MODELS = "/shanoir-ng/preclinical/pathology/model";

	private static final String GET_PATHOLOGIES = "/shanoir-ng/preclinical/pathology/";

	private static final String GET_THERAPIES = "/shanoir-ng/preclinical/therapy/";

	private static final String CREATE_ANESTHETIC = "/shanoir-ng/preclinical/anesthetic/";

	private static final String CREATE_ANIMAL_SUBJECT = "/shanoir-ng/preclinical/subject/";

	private static final String CREATE_THERAPY = "/shanoir-ng/preclinical/therapy/";

	private static final String CREATE_SUBJECT_THERAPY = "/shanoir-ng/preclinical/subject/{id}/therapy/";

	private static final String CREATE_SUBJECT_PATHOLOGY = "/shanoir-ng/preclinical/subject/{id}/pathology/";

	private static final String CREATE_PATHOLOGY_MODEL= "/shanoir-ng/preclinical/pathology/model/";

	private static final String CREATE_EXAMINATION_ANESTHETIC = "/shanoir-ng/preclinical/examination/{id}/anesthetic/";

	private static final String CREATE_PATHOLOGY = "/shanoir-ng/preclinical/pathology/";

	private static final String CREATE_REFERENCE = "/shanoir-ng/preclinical/refs/";

	private static final String CREATE_INGREDIENT = "/shanoir-ng/preclinical/anesthetic/{id}/ingredient/";

	public List<Reference> getPreclinicalReferences() throws ShanoirException {
		try {
			ResponseEntity<Reference[]> response = this.distantKeycloak.getRestTemplate().exchange(getURI(GET_REFERENCES), HttpMethod.GET, new HttpEntity<>(getHeader()), Reference[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant Reference {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant Reference: ", e);
		}
	}

	public List<Anesthetic> getAnesthetics() throws ShanoirException {
		try {
			ResponseEntity<Anesthetic[]> response = this.distantKeycloak.getRestTemplate().exchange(getURI(GET_ANESTHETICS), HttpMethod.GET, new HttpEntity<>(getHeader()), Anesthetic[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant Anesthetic {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant Anesthetic: ", e);
		}
	}

	public List<PathologyModel> getPathologyModels() throws ShanoirException {
		try {
			ResponseEntity<PathologyModel[]> response = this.distantKeycloak.getRestTemplate().exchange(getURI(GET_PATHOLOGY_MODELS), HttpMethod.GET, new HttpEntity<>(getHeader()), PathologyModel[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant PathologyModel {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant PathologyModel: ", e);
		}
	}

	public List<Pathology> getPathologies() throws ShanoirException {
		try {
			ResponseEntity<Pathology[]> response = this.distantKeycloak.getRestTemplate().exchange(getURI(GET_PATHOLOGIES), HttpMethod.GET, new HttpEntity<>(getHeader()), Pathology[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant Pathology {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant Pathology: ", e);
		}
	}

	public List<Therapy> getTherapies() throws ShanoirException {
		try {
			ResponseEntity<Therapy[]> response = this.distantKeycloak.getRestTemplate().exchange(getURI(GET_THERAPIES), HttpMethod.GET, new HttpEntity<>(getHeader()), Therapy[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant Therapy {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant Therapy: ", e);
		}
	}

	public AnimalSubject createAnimalSubject(AnimalSubject animalSubject) throws ShanoirException {
		try {
			ResponseEntity<AnimalSubject> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_ANIMAL_SUBJECT), HttpMethod.POST, new HttpEntity<>(animalSubject, getHeader()), AnimalSubject.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant AnimalSubject {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant AnimalSubject: ", e);
		}
	}

	public Therapy createTherapy(Therapy therapy) throws ShanoirException {
		try {
			ResponseEntity<Therapy> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_THERAPY), HttpMethod.POST, new HttpEntity<>(therapy, getHeader()), Therapy.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant Therapy {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant Therapy: ", e);
		}
	}

	public Anesthetic createAnesthetic(Anesthetic anesthetic) throws ShanoirException {
		try {
			ResponseEntity<Anesthetic> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_ANESTHETIC), HttpMethod.POST, new HttpEntity<>(anesthetic, getHeader()), Anesthetic.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant anesthetic {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant anesthetic: ", e);
		}
	}

	public SubjectTherapy createSubjectTherapy(SubjectTherapy subjecttherap, Long subjectId) throws ShanoirException {
		try {
			ResponseEntity<SubjectTherapy> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_SUBJECT_THERAPY.replace("{id}", subjectId.toString())), HttpMethod.POST, new HttpEntity<>(subjecttherap, getHeader()), SubjectTherapy.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant SubjectTherapy {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant SubjectTherapy: ", e);
		}
	}

	public SubjectPathology createSubjectPathology(SubjectPathology subjectPatho, Long subjectId) throws ShanoirException {
		try {
			ResponseEntity<SubjectPathology> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_SUBJECT_PATHOLOGY.replace("{id}", subjectId.toString())), HttpMethod.POST, new HttpEntity<>(subjectPatho, getHeader()), SubjectPathology.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant SubjectPathology {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant SubjectPathology: ", e);
		}
	}

	public PathologyModel createPathologyModel(PathologyModel pathologyModel) throws ShanoirException {
		try {
			ResponseEntity<PathologyModel> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_PATHOLOGY_MODEL), HttpMethod.POST, new HttpEntity<>(pathologyModel, getHeader()), PathologyModel.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant PathologyModel {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant PathologyModel: ", e);
		}
	}

	public ExaminationAnesthetic createExaminationAnesthatic(ExaminationAnesthetic examAnes, Long examId) throws ShanoirException {
		try {
			LOG.error("examId " +  examId);

			ResponseEntity<ExaminationAnesthetic> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_EXAMINATION_ANESTHETIC.replace("{id}", "" + examId)), HttpMethod.POST, new HttpEntity<>(examAnes, getHeader()), ExaminationAnesthetic.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant ExaminationAnesthetic {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant ExaminationAnesthetic: ", e);
		}
	}

	public Pathology createPathology(Pathology pathology) throws ShanoirException {
		try {
			ResponseEntity<Pathology> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_PATHOLOGY), HttpMethod.POST, new HttpEntity<>(pathology, getHeader()), Pathology.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant Pathology {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant Pathology: ", e);
		}
	}

	public Reference createReference(Reference ref) throws ShanoirException {
		try {
			ResponseEntity<Reference> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_REFERENCE), HttpMethod.POST, new HttpEntity<>(ref, getHeader()), Reference.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant Reference {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant Reference: ", e);
		}
	}

	public AnestheticIngredient createIngredient(AnestheticIngredient ingredient, Long anestheticId) throws ShanoirException {
		try {
			ResponseEntity<AnestheticIngredient> response = this.distantKeycloak.getRestTemplate().exchange(getURI(CREATE_INGREDIENT.replace("{id}", anestheticId.toString())), HttpMethod.POST, new HttpEntity<>(ingredient, getHeader()), AnestheticIngredient.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant ingredient {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant ingredient: ", e);
		}
	}

	private HttpHeaders getHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + distantKeycloak.getAccessToken());
		return headers;
	}

	public URI getURI(String apiHeader) throws URISyntaxException {
		return new URI(distantKeycloak.getServer() + apiHeader);
	}

}
