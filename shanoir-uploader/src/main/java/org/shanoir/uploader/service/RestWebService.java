/**
 * 
 */
package org.shanoir.uploader.service;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.keycloak.representations.AccessTokenResponse;
import org.shanoir.uploader.ShUpConfig;
import org.shanoir.uploader.model.ExportData;
import org.shanoir.uploader.model.dto.CenterDTO;
import org.shanoir.uploader.model.dto.EquipmentDicom;
import org.shanoir.uploader.model.dto.ExaminationDTO;
import org.shanoir.uploader.model.dto.InvestigatorDTO;
import org.shanoir.uploader.model.dto.SimpleStudyCardDTO;
import org.shanoir.uploader.model.dto.SimpleStudyDTO;
import org.shanoir.uploader.model.dto.StudyCardDTO;
import org.shanoir.uploader.model.dto.StudyDTO;
import org.shanoir.uploader.model.dto.SubjectDTO;
import org.shanoir.uploader.model.dto.SubjectStudyDTO;
import org.shanoir.uploader.model.dto.rest.IdNameDTO;
import org.shanoir.uploader.model.dto.rest.SubjectFromShupDTO;
import org.shanoir.uploader.service.keycloak.KeycloakClient;
import org.shanoir.uploader.service.keycloak.KeycloakConfiguration;
import org.shanoir.uploader.service.wsdl.ShanoirUploaderServiceClient;
import org.shanoir.uploader.utils.Util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author atouboul
 *
 */
public class RestWebService implements IWebService {

	/** Instance unique pré-initialisée */
	private static RestWebService INSTANCE = new RestWebService();

	private static Logger logger = Logger.getLogger(RestWebService.class);

	private KeycloakConfiguration keycloakConfig = KeycloakConfiguration.getInstance();

	private AccessTokenResponse tokenReponse;

	private RestWebService() {
	}

	public static IWebService getInstance() {
		return INSTANCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.uploader.service.WebServiceItf#init()
	 */
	public Integer init() {
		keycloakConfig.setKeycloakRequestsRealm(
				ShUpConfig.shanoirNGServerProperties.getProperty("shanoir.server.keycloak.realm"));
		keycloakConfig.setKeycloakRequestsUserLogin(
				ShUpConfig.shanoirNGServerProperties.getProperty("shanoir.server.user.name"));
		keycloakConfig.setKeycloakRequestsUserPassword(
				ShUpConfig.shanoirNGServerProperties.getProperty("shanoir.server.user.password"));
		keycloakConfig.setKeycloakRequestsAuthServerUrl(
				ShUpConfig.shanoirNGServerProperties.getProperty("shanoir.server.keycloak.host"));
		keycloakConfig.setKeycloakRequestsClientId(
				ShUpConfig.shanoirNGServerProperties.getProperty("shanoir.server.keycloak.client"));
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.shanoir.uploader.service.WebServiceItf#testConnection()
	 */
	public Integer testConnection() {
		WebServiceResponse<AccessTokenResponse> response = null;
		// 401 : token expired
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(
					keycloakConfig.getKeycloakRequestsAuthServerUrl()).openConnection();
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			switch (Integer.parseInt(Integer.toString(responseCode).substring(0, 1))) {
			case 2:
				response = KeycloakClient.getAccessToken(keycloakConfig, false);
				switch (response.getStatusCode()) {
				case 0:
					
					tokenReponse = response.getObj();
					return 0;
				case -1:
					return -1;
				default:
					return -3;
				}
			default:
				return -3;
			}

			// InetAddress.getByName(keycloakConfig.getKeycloakRequestsAuthServerUrl()).isReachable(3000);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -3;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -3;
		}

	}

	public WebServiceResponse<List<StudyDTO>> findStudies(EquipmentDicom equipment) {
		// TODO Auto-generated method stub
		validateOrRefreshToken();
		String url = ShUpConfig.shanoirNGServerProperties
				.getProperty("shanoir.server.find.study.service.url");
		HttpClient client = HttpClientBuilder.create().build();
		StringEntity entity = new StringEntity(equipment.payload(), ContentType.APPLICATION_JSON);
		HttpPost request = new HttpPost(url);
		// add request header
		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
		request.setEntity(entity);

		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<SimpleStudyDTO> listSimpleStudyDTO  = Util.getMappedList(response,SimpleStudyDTO.class);
		
		WebServiceResponse<List<StudyDTO>> wsResponse = new WebServiceResponse<List<StudyDTO>>();
		
		if (listSimpleStudyDTO != null) {
			List<StudyDTO> studyDTOList = new ArrayList<StudyDTO>();
			for (SimpleStudyDTO s : listSimpleStudyDTO) {
				List<CenterDTO> centerList = new ArrayList<CenterDTO>();
				List<StudyCardDTO> studyCardList = new ArrayList<StudyCardDTO>();
				// Centers part
				if (s.getCenters() != null) {
					List<InvestigatorDTO> investigatorList = new ArrayList<InvestigatorDTO>();
					investigatorList.add(new InvestigatorDTO(0, "~~ NOT IMPLEMENTED YET IN SHANOIR NG ~~"));
					for (IdNameDTO c : s.getCenters()) {
//						if (c.getInvestigators() != null) {
//							List<InvestigatorDTO> investigatorList = new ArrayList<InvestigatorDTO>();
//							for (org.shanoir.ws.generated.Find.Study.InvestigatorDTO i : c.getInvestigators()){
//								investigatorList.add(new InvestigatorDTO(i.getId(),i.getName()));
//							}<>
//							centerList.add(new CenterDTO(c.getId(),c.getName(),investigatorList));
//						} else {
							centerList.add(new CenterDTO((long) c.getId().intValue(),c.getName(),investigatorList));								
//						}
					}
				}
				
				// StudyCards part
				if (s.getStudyCards() != null) {
					for (SimpleStudyCardDTO sc : s.getStudyCards()){
						if (sc.getCompatible()){
							studyCardList.add(new StudyCardDTO((long) sc.getId().intValue(),sc.getName(),(long) sc.getCenter().getId().intValue(),sc.getCenter().getName(),equipment.getManufacturer(),equipment.getManufacturerModelName(),equipment.getDeviceSerialNumber()));
						} else {
							studyCardList.add(new StudyCardDTO((long) sc.getId().intValue(),sc.getName(),(long) sc.getCenter().getId().intValue(),sc.getCenter().getName(),"","",""));
						}
					}
				}
				
				//Study part
				studyDTOList.add(new StudyDTO((long) s.getId().intValue(),s.getName(),studyCardList,centerList));
			}
			wsResponse.setObj(studyDTOList);

		}
		return wsResponse;
	}

	public WebServiceResponse<Long> createSubject(ExportData exportData) {
		validateOrRefreshToken();
		String url = ShUpConfig.shanoirNGServerProperties
				.getProperty("shanoir.server.create.subject.service.url");
		HttpClient client = HttpClientBuilder.create().build();
		SubjectFromShupDTO subjectFromShupDTO = new SubjectFromShupDTO(exportData);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String payload = null;
		try {
			payload = ow.writeValueAsString(subjectFromShupDTO);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
		HttpPost request = new HttpPost(url);
		// add request header
		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
		request.setEntity(entity);

		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		Long subjectId = Util.getMappedObject(response,Long.class);
		WebServiceResponse<Long> wsResponse = new WebServiceResponse<Long>();
		
		if (subjectId != null) {
			wsResponse.setObj(subjectId);
		} else {
			wsResponse.setObj(null);
			wsResponse.setStatusCode(-6);
			wsResponse.setStatus("SUBJECT_PERSIST_FAIL");
		}

		return wsResponse;
	}

	public WebServiceResponse<Long> updateSubject(ExportData exportData) {
		validateOrRefreshToken();
		String url = ShUpConfig.shanoirNGServerProperties
				.getProperty("shanoir.server.update.subject.service.url")+"/"+exportData.getSubject().getId();
		HttpClient client = HttpClientBuilder.create().build();
		SubjectFromShupDTO subjectFromShupDTO = new SubjectFromShupDTO(exportData);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String payload = null;
		try {
			payload = ow.writeValueAsString(subjectFromShupDTO);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
		HttpPut request = new HttpPut(url);
		// add request header
		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
		request.setEntity(entity);

		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		Long subjectId = Util.getMappedObject(response,Long.class);
		WebServiceResponse<Long> wsResponse = new WebServiceResponse<Long>();
		
		if (subjectId != null) {
			wsResponse.setObj(subjectId);
		} else {
			wsResponse.setObj(null);
			wsResponse.setStatusCode(-6);
			wsResponse.setStatus("SUBJECT_UPDATE_FAIL");
		}

		return wsResponse;
	}

	public WebServiceResponse<Long> createExamination(ExportData exportData, Long subjectId) {
		validateOrRefreshToken();
		String url = ShUpConfig.shanoirNGServerProperties
				.getProperty("shanoir.server.create.examination.service.url");
		HttpClient client = HttpClientBuilder.create().build();
		org.shanoir.uploader.model.dto.rest.ExaminationDTO examinationDTO = new org.shanoir.uploader.model.dto.rest.ExaminationDTO(exportData,subjectId);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String payload = null;
		try {
			payload = ow.writeValueAsString(examinationDTO);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_JSON);
		HttpPost request = new HttpPost(url);
		// add request header
		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
		request.setEntity(entity);

		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
		Long examinationId = Util.getMappedObject(response,Long.class);
		WebServiceResponse<Long> wsResponse = new WebServiceResponse<Long>();
		
		if (examinationId != null) {
			wsResponse.setObj(examinationId);
		} else {
			wsResponse.setObj(null);
			wsResponse.setStatusCode(-6);
			wsResponse.setStatus("EXAM_PERSIST_FAIL");
		}
		
		return wsResponse;
	}

	public WebServiceResponse<SubjectDTO> findSubjectByIdentifier(String subjectIdentifier) {
		validateOrRefreshToken();
		String url = ShUpConfig.shanoirNGServerProperties
				.getProperty("shanoir.server.find.subject.service.url")+subjectIdentifier;
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		// add request header
		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
//		request.setEntity(entity);

		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		WebServiceResponse<SubjectDTO> wsResponse = new WebServiceResponse<SubjectDTO>();
		
		switch (response.getStatusLine().getStatusCode()) {
			case 200 : 
				org.shanoir.uploader.model.dto.rest.SubjectDTO subjectDTO = Util.getMappedObject(response,org.shanoir.uploader.model.dto.rest.SubjectDTO.class);
				List<SubjectStudyDTO> subjectStudyDTO = new ArrayList<SubjectStudyDTO>();
				if (subjectDTO.getSubjectStudyList() != null){
					for (org.shanoir.uploader.model.dto.rest.SubjectStudyDTO ssd : subjectDTO.getSubjectStudyList()){
						subjectStudyDTO.add(new SubjectStudyDTO(ssd.getId(), ssd.getStudy().getId(), ssd.isPhysicallyInvolved(), ssd.getSubjectStudyIdentifier(), subjectTypeNGToOLD(ssd.getSubjectType().getId())));
					}
				}
				SubjectDTO foundSubject = new SubjectDTO(subjectDTO.getId(), Util.toXMLGregorianCalendar(subjectDTO.getBirthDate()), subjectDTO.getName(), sexNGToOld(subjectDTO.getSex().getId()), imageObjectCategoryNGToOLD(subjectDTO.getImagedObjectCategory().getId()), hemisphericDominanceNGToOLD(subjectDTO.getLanguageHemisphericDominance().getId()), hemisphericDominanceNGToOLD(subjectDTO.getManualHemisphericDominance().getId()), subjectStudyDTO, subjectIdentifier);
				//wsResponse.setObj(Util.getMappedObject(response,org.shanoir.uploader.model.dto.rest.SubjectDTO.class));
				wsResponse.setObj(foundSubject);
			  	break;
			case 204 : 
				wsResponse.setObj(null);
				break;
			case 401 :
			case 404 :
			case 403 :
			case 500 : 
				wsResponse.setObj(null);
				wsResponse.setStatusCode(-1);
				break;
			default:
				wsResponse.setObj(null);
				wsResponse.setStatusCode(-1);
				break;
				   
		}
		
		return wsResponse;
		

	}

	public WebServiceResponse<List<ExaminationDTO>> findExaminationsBySubjectId(Long subjectId) {
		validateOrRefreshToken();
		String url = ShUpConfig.shanoirNGServerProperties
				.getProperty("shanoir.server.find.examination.service.url")+subjectId;
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		// add request header
		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());
//		request.setEntity(entity);

		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		WebServiceResponse<List<ExaminationDTO>> wsResponse = new WebServiceResponse<List<ExaminationDTO>>();
		switch (response.getStatusLine().getStatusCode()) {
		case 200 : 
			List<org.shanoir.uploader.model.dto.rest.ExaminationDTO> examinationDTOList = Util.getMappedList(response,org.shanoir.uploader.model.dto.rest.ExaminationDTO.class);
			List<ExaminationDTO> examinationDTOs = new ArrayList<ExaminationDTO>();
			for (org.shanoir.uploader.model.dto.rest.ExaminationDTO e : examinationDTOList){
				examinationDTOs.add(new ExaminationDTO(e.getId(), Util.toXMLGregorianCalendar(e.getExaminationDate()), e.getComment()));
			}
			wsResponse.setObj(examinationDTOs);
		  	break;
		case 204 : 
			wsResponse.setObj(null);
			break;
		case 401 :
		case 404 :
		case 403 :
		case 500 : 
			wsResponse.setObj(null);
			wsResponse.setStatusCode(-1);
			break;
		default:
			wsResponse.setObj(null);
			wsResponse.setStatusCode(-1);
			break;
			   
	}
	
	return wsResponse;
	
	}

	public void validateOrRefreshToken() {
		if (!KeycloakClient.verifyToken(keycloakConfig, tokenReponse)) {
			tokenReponse = KeycloakClient.getAccessToken(keycloakConfig, true).getObj();
		}
		;
	}

	public String subjectTypeNGToOLD(Integer subjectTypeId) {
		switch (subjectTypeId) {
			case 1 : return "Healthy volunteer";
			case 2 : return "Patient";
			case 3 : return "Phantom";
			default : return null;
		}
	}
	
	public String imageObjectCategoryNGToOLD(Integer iOC) {
		switch (iOC) {
			case 1 : return "Phantom";
			case 2 : return "Living human being";
			case 3 : return "Human cadaver";
			case 4 : return "Anatomical piece";
			default : return null;
		}
	}
	
	public String hemisphericDominanceNGToOLD(Integer hemisphericDominance) {
		switch (hemisphericDominance) {
			case 1 : return "Left";
			case 2 : return "Right";
			default : return null;
		}
	}
	
	public String sexNGToOld(Integer sex) {
		switch (sex) {
			case 1 : return "M";
			case 2 : return "F";
			default : return null;
		}
	}

	public void uploadFile(File file, File folder) throws Exception {
		validateOrRefreshToken();
		final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
		final String CONTENT_TYPE = "application/zip";
		final String BOUNDARY = "--import_from_shup--";

		String url = ShUpConfig.shanoirNGServerProperties
				.getProperty("shanoir.server.upload.service.url");

		HttpEntity entity = MultipartEntityBuilder
			    .create()
			    .addBinaryBody("file", file, ContentType.create("application/octet-stream"), "data.zip")
			    .build();

		HttpClient client = HttpClientBuilder.create().build();
		HttpPost request = new HttpPost(url);
		request.setEntity(entity);

		request.addHeader("Authorization", "Bearer " + tokenReponse.getToken());


		HttpResponse response = null;
		try {
			response = client.execute(request);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}

	public ShanoirUploaderServiceClient getShanoirUploaderService() {
		return null;
	}
	
}
