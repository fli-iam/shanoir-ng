package org.shanoir.ng.migration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.shanoir.ng.acquisitionequipment.model.AcquisitionEquipment;
import org.shanoir.ng.center.model.Center;
import org.shanoir.ng.manufacturermodel.model.Manufacturer;
import org.shanoir.ng.manufacturermodel.model.ManufacturerModel;
import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.shanoir.ng.study.dto.StudyDTO;
import org.shanoir.ng.study.dto.mapper.StudyMapper;
import org.shanoir.ng.study.model.Study;
import org.shanoir.ng.study.service.StudyService;
import org.shanoir.ng.studycenter.StudyCenter;
import org.shanoir.ng.subject.model.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class DistantStudiesShanoirService {

	private static final Logger LOG = LoggerFactory.getLogger(DistantStudiesShanoirService.class);

	private static final String SERVICE_SUBJECTS_CREATE = "/shanoir-ng/studies/subjects";

	private static final String SERVICE_STUDY_CREATE = "/shanoir-ng/studies/studies";

	private static final String ADD_PROTOCOL_FILE_PATH = "/shanoir-ng/studies/studies/protocol-file-upload/";

	private static final String GET_CENTERS = "/shanoir-ng/studies/centers/names";

	private static final String CREATE_CENTER = "/shanoir-ng/studies/centers";

	private static final String CREATE_EQUIPEMENT = "/shanoir-ng/studies/acquisitionequipments";

	private static final String CREATE_MODEL = "/shanoir-ng/studies/manufacturermodels";

	private static final String CREATE_MANUFACTURER = "/shanoir-ng/studies/manufacturers";

	private static final String GET_EQUIPEMENTS = "/shanoir-ng/studies/acquisitionequipments";

	private static final String GET_MODELS = "/shanoir-ng/studies/manufacturermodels";

	private static final String GET_MANUFACTURERS = "/shanoir-ng/studies/manufacturers";

	RestTemplate restTemplate;

	@Autowired
	StudyService studyService;

	@Autowired
	StudyMapper mapper;

	@Autowired
	DistantKeycloakConfigurationService distantKeycloak;

	private static final TrustManager[] UNQUESTIONING_TRUST_MANAGER = new TrustManager[]{
			new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers(){
					return null;
				}
				@Override
				public void checkClientTrusted( X509Certificate[] certs, String authType ){}
				@Override
				public void checkServerTrusted( X509Certificate[] certs, String authType ){}
			}
	};

	public DistantStudiesShanoirService() {
		// Instanciate a "weak" rest template.
		TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

		SSLContext sslContext;
		try {
			sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy)
					.build();

			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

			CloseableHttpClient httpClient = HttpClients.custom()
					.setSSLSocketFactory(csf)
					.build();

			HttpComponentsClientHttpRequestFactory requestFactory =
					new HttpComponentsClientHttpRequestFactory();

			requestFactory.setHttpClient(httpClient);
			restTemplate = new RestTemplate(requestFactory);
		} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
			// POUTOU
		}
	}

	/**
	 * Creates a subject in the distant shanoir instance
	 * @param subject the subject to create
	 * @throws ShanoirException
	 */
	public Subject createSubject(Subject subject) throws ShanoirException {
		try {
			ResponseEntity<Subject> response = this.restTemplate.exchange(getURI(SERVICE_SUBJECTS_CREATE), HttpMethod.POST, new HttpEntity<>(subject, getHeader()), Subject.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant subject {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant subject: ", e);
		}
	}

	/**
	 * Creates a study in the distant shanoir instance
	 * @param study the study to create
	 * @throws ShanoirException
	 */
	public StudyDTO createStudy(Study study) throws ShanoirException {
		try {
			StudyDTO dto = mapper.studyToStudyDTO(study);
			ResponseEntity<StudyDTO> response = this.restTemplate.exchange(getURI(SERVICE_STUDY_CREATE), HttpMethod.POST, new HttpEntity<>(dto, getHeader()), StudyDTO.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant study  {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant study: ", e);
		}
	}

	public Study updateStudy(Study study) throws ShanoirException {
		try {
			Study newStudy = new Study();
			for (StudyCenter studyCenter : study.getStudyCenterList()) {
				// Avoid infinite recursion on Center <=> Equipment <=> Study
				Center cent = studyCenter.getCenter();
				Center newCenter = new Center();
				newCenter.setId(cent.getId());
				studyCenter.setCenter(newCenter);
				studyCenter.setStudy(newStudy);
			}
			ResponseEntity<Study> response = this.restTemplate.exchange(getURI(SERVICE_STUDY_CREATE + "/" + study.getId()), HttpMethod.POST, new HttpEntity<>(study, getHeader()), Study.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant study  {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant study: ", e);

		}
	}

	/**
	 * Creates a study in the distant shanoir instance
	 * @param studyId the old study ID where to create the new file
	 * @param file the file to send
	 * @param studyId the study Id
	 * @throws ShanoirException
	 */
	public void addProtocoleFile(File file, String studyId) throws ShanoirException {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.add("Authorization", "Bearer " + distantKeycloak.getAccessToken());

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", file);

			HttpEntity<MultiValueMap<String, Object>> requestEntity	= new HttpEntity<>(body, headers);

			restTemplate.postForEntity(getURI(ADD_PROTOCOL_FILE_PATH + studyId), requestEntity, Void.class);
		} catch (Exception e) {
			throw new ShanoirException("Could not add protocol file on study: ", e);
		}
	}

	/**
	 * Get the IdName list of all centers.
	 * @throws ShanoirException
	 */
	public List<IdName> getAllCenters() throws ShanoirException {
		try {
			ResponseEntity<IdName[]> response = this.restTemplate.exchange(getURI(GET_CENTERS), HttpMethod.GET, new HttpEntity<>(getHeader()), IdName[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant centers {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant centers: ", e);

		}
	}

	/**
	 * Create a new center
	 * @param center
	 * @throws ShanoirException
	 */
	public Center createCenter(Center center) throws ShanoirException {
		try {
			ResponseEntity<Center> response = this.restTemplate.exchange(getURI(CREATE_CENTER), HttpMethod.POST, new HttpEntity<Center>(center, getHeader()), Center.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant center  {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant center: ", e);

		}
	}

	public List<AcquisitionEquipment> getAcquisitionEquipements() throws ShanoirException {
		try {
			ResponseEntity<AcquisitionEquipment[]> response = this.restTemplate.exchange(getURI(GET_EQUIPEMENTS), HttpMethod.GET, new HttpEntity<>(getHeader()), AcquisitionEquipment[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant equipements {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant equipements: ", e);

		}
	}

	public List<ManufacturerModel> getModels() throws ShanoirException {
		try {
			ResponseEntity<ManufacturerModel[]> response = this.restTemplate.exchange(getURI(GET_MODELS), HttpMethod.GET, new HttpEntity<>(getHeader()), ManufacturerModel[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant models {} {}" + response.getStatusCode() + response.getBody());

			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant models: ", e);

		}
	}

	public List<Manufacturer> getManufacturers() throws ShanoirException {
		try {
			ResponseEntity<Manufacturer[]> response = this.restTemplate.exchange(getURI(GET_MANUFACTURERS), HttpMethod.GET, new HttpEntity<>(getHeader()), Manufacturer[].class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return new ArrayList<>(Arrays.asList(response.getBody()));
			} else {
				throw new ShanoirException("Could not retrieve distant manufacturers {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not retrieve distant manufacturers: ", e);

		}
	}

	public AcquisitionEquipment createEquipement(AcquisitionEquipment equipement) throws ShanoirException {
		try {
			ResponseEntity<AcquisitionEquipment> response = this.restTemplate.exchange(getURI(CREATE_EQUIPEMENT), HttpMethod.POST, new HttpEntity<AcquisitionEquipment>(equipement, getHeader()), AcquisitionEquipment.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant equipement  {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant equipement: ", e);
		}
	}

	public Manufacturer createManufacturer(Manufacturer manufacturer) throws ShanoirException {
		try {
			ResponseEntity<Manufacturer> response = this.restTemplate.exchange(getURI(CREATE_MANUFACTURER), HttpMethod.POST, new HttpEntity<Manufacturer>(manufacturer, getHeader()), Manufacturer.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant manufacturer  {} {}" + response.getStatusCode() + response.getBody());

			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant manufacturer: ", e);
		}
	}

	public ManufacturerModel createManufacturerModel(ManufacturerModel manufacturerModel) throws ShanoirException {
		try {
			ResponseEntity<ManufacturerModel> response = this.restTemplate.exchange(getURI(CREATE_MODEL), HttpMethod.POST, new HttpEntity<ManufacturerModel>(manufacturerModel, getHeader()), ManufacturerModel.class);
			if (HttpStatus.OK.equals(response.getStatusCode())) {
				return response.getBody();
			} else {
				throw new ShanoirException("Could not create a new distant model  {} {}" + response.getStatusCode() + response.getBody());
			}
		} catch (Exception e) {
			throw new ShanoirException("Could not create a new distant model: ", e);

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
