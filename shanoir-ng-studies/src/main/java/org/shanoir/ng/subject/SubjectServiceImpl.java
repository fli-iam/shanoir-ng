package org.shanoir.ng.subject;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMQConfiguration;
import org.shanoir.ng.shared.dto.IdNameDTO;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.exception.ShanoirStudiesException;
import org.shanoir.ng.shared.exception.StudiesErrorModelCode;
import org.shanoir.ng.shared.service.MicroserviceRequestsService;
import org.shanoir.ng.study.StudyRepository;
import org.shanoir.ng.study.StudyService;
import org.shanoir.ng.subject.dto.SimpleSubjectDTO;
import org.shanoir.ng.subject.dto.SubjectFromShupDTO;
import org.shanoir.ng.subjectstudy.ExaminationDTO;
import org.shanoir.ng.subjectstudy.SubjectStudy;
import org.shanoir.ng.subjectstudy.SubjectStudyDecorator;
import org.shanoir.ng.subjectstudy.SubjectStudyRepository;
import org.shanoir.ng.utils.KeycloakUtil;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Subject service implementation.
 *
 * @author msimon
 *
 */
@Service
public class SubjectServiceImpl implements SubjectService {

	/**
	 * Logger
	 */
	private static final Logger LOG = LoggerFactory.getLogger(SubjectServiceImpl.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private SubjectStudyRepository subjectStudyRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MicroserviceRequestsService microservicesRequestsService;
	
	@Autowired
	private SubjectStudyDecorator subjectStudyMapper;
	
	@Autowired
	private StudyService studyService;

	@Override
	public void deleteById(final Long id) throws ShanoirStudiesException {
		subjectRepository.delete(id);
	}

	@Override
	public List<Subject> findAll() {
		return Utils.toList(subjectRepository.findAll());
	}
	
	@Override
	public List<IdNameDTO> findIdsAndNames() {
		return subjectRepository.findIdsAndNames();
	}

	@Override
	public List<Subject> findBy(final String fieldName, final Object value) {
		return subjectRepository.findBy(fieldName, value);
	}

	@Override
	public Optional<Subject> findByData(final String name) {
		return subjectRepository.findByName(name);
	}

	@Override
	public Subject findById(final Long id) {
		return subjectRepository.findOne(id);
	}

	@Override
	public Subject save(final Subject subject) throws ShanoirStudiesException {
		try {
			for (final SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
				subjectStudy.setSubject(subject);
			}
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating subject", dive);
			throw new ShanoirStudiesException("Error while creating subject");
		}
		return subjectRepository.save(subject);
	}

	@Override
	public Subject saveForOFSEP(final Subject subject, final Long studyCardId) throws ShanoirStudiesException {
		Subject savedSubject = null;
		String commonName = createOfsepCommonName(studyCardId);
		if (commonName == null || commonName.equals(""))
			subject.setName("NoCommonName");
		else
			subject.setName(commonName);
		try {
			savedSubject = subjectRepository.save(subject);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating subject", dive);
			throw new ShanoirStudiesException("Error while creating subject");
		}
		// updateShanoirOld(savedSubject);
		return savedSubject;
	}

	@Override
	public Subject saveFromJson(final File jsonFile) throws ShanoirStudiesException {

		ObjectMapper mapper = new ObjectMapper();
		Subject subject = new Subject();
		try {
			subject = mapper.readValue(jsonFile, Subject.class);
		} catch (IOException e) {
			LOG.error("Error while reading subject json file", e);
			throw new ShanoirStudiesException("Error while creating subject");
		}

		Subject savedSubject = null;
		try {
			savedSubject = subjectRepository.save(subject);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating subject", dive);
			throw new ShanoirStudiesException("Error while creating subject");
		}
		updateShanoirOld(savedSubject);
		return savedSubject;
	}

	@Override
	public Subject update(final Subject subject) throws ShanoirStudiesException {
		final Subject subjectDb = subjectRepository.findOne(subject.getId());
		updateSubjectValues(subjectDb, subject);
		try {
			subjectRepository.save(subjectDb);
		} catch (Exception e) {
			LOG.error("Error while updating subject", e);
			throw new ShanoirStudiesException("Error while updating subject");
		}
		updateShanoirOld(subjectDb);
		return subjectDb;
	}
	
	@Override
	public Subject update(final Subject subject, final SubjectFromShupDTO subjectFromShupDTO) throws ShanoirStudiesException {
	
		for (SubjectStudy ss : subject.getSubjectStudyList()) {
				LOG.error("<INIT> Suject id " + ss.getId());
				LOG.error("<INIT> Suject/study id " + ss.getSubjectStudyIdentifier());
				LOG.error("<INIT> Suject Type " + ss.getSubjectType().name());
				LOG.error("<INIT> physically involved " + ss.isPhysicallyInvolved());
		}

		Subject subjectUpdated = updateSubjectValues(subject, subjectFromShupDTO);
		for (SubjectStudy ss : subjectUpdated.getSubjectStudyList()) {
			//if (ss.getId() == subjectFromShupDTO.getStudyId()) {
				LOG.error("<AFTER> Suject id " + ss.getId());
				LOG.error("<AFTER> Suject/study id " + ss.getSubjectStudyIdentifier());
				LOG.error("<AFTER> Suject Type " + ss.getSubjectType().name());
				LOG.error("<AFTER> physically involved " + ss.isPhysicallyInvolved());

		//	}
		}
		try {
			subjectRepository.save(subjectUpdated);
		} catch (Exception e) {
			LOG.error("Error while updating subject", e);
			throw new ShanoirStudiesException("Error while updating subject");
		}
		updateShanoirOld(subjectUpdated);
		return subjectUpdated;
	}

	@Override
	public void updateFromShanoirOld(final Subject subject) throws ShanoirStudiesException {
		if (subject.getId() == null) {
			throw new IllegalArgumentException("Subject id cannot be null");
		} else {
			final Subject subjectDb = subjectRepository.findOne(subject.getId());
			if (subjectDb != null) {
				try {
					subjectDb.setName(subject.getName());
					subjectRepository.save(subjectDb);
				} catch (Exception e) {
					LOG.error("Error while updating subject from Shanoir Old", e);
					throw new ShanoirStudiesException("Error while updating subject from Shanoir Old");
				}
			}
		}
	}

	/*
	 * Update Shanoir Old.
	 *
	 * @param template template.
	 *
	 * @return false if it fails, true if it succeed.
	 */
	@Override
	public boolean updateShanoirOld(final Subject subject) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMQConfiguration.subjectQueueOut().getName(),
					new ObjectMapper().writeValueAsString(subject));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send Subject " + subject.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMQConfiguration.studyQueueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send Subject " + subject.getId()
					+ " save/update because of an error while serializing Subject.", e);
		}
		return false;
	}

	/*
	 * Update some values of template to save them in database.
	 *
	 * @param templateDb template found in database.
	 *
	 * @param template template with new values.
	 *
	 * @return database template with new values.
	 */
	private Subject updateSubjectValues(final Subject subjectDb, final Subject subject) {

		subjectDb.setName(subject.getName());
		//subjectDb.setBirthDate(subject.getBirthDate());
		subjectDb.setIdentifier(subject.getIdentifier());
		subjectDb.setPseudonymusHashValues(subject.getPseudonymusHashValues());
		subjectDb.setSex(subject.getSex());
		subjectDb.setSubjectStudyList(subject.getSubjectStudyList());
		subjectDb.setManualHemisphericDominance(subject.getManualHemisphericDominance());
		subjectDb.setLanguageHemisphericDominance(subject.getLanguageHemisphericDominance());
		subjectDb.setImagedObjectCategory(subject.getImagedObjectCategory());
		subjectDb.setUserPersonalCommentList(subject.getUserPersonalCommentList());
		
		// Copy list of database links subject/study
		final List<SubjectStudy> subjectStudyDbList = new ArrayList<>(subjectDb.getSubjectStudyList());
		for (final SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
			if (subjectStudy.getId() == null) {
				// Add link subject/study
				subjectStudy.setSubject(subjectDb);
				subjectDb.getSubjectStudyList().add(subjectStudy);
			}
		}
		for (final SubjectStudy subjectStudyDb : subjectStudyDbList) {
			boolean keepSubjectStudy = false;
			for (final SubjectStudy subjectStudy : subject.getSubjectStudyList()) {
				if (subjectStudyDb.getId().equals(subjectStudy.getId())) {
					keepSubjectStudy = true;
					break;
				}
			}
			if (!keepSubjectStudy) {
				// Move link subject/study
				subjectDb.getSubjectStudyList().remove(subjectStudyDb);
				subjectStudyRepository.delete(subjectStudyDb.getId());
			}
		}
		return subjectDb;
	}
	
	/*
	 * Update some values of template to save them in database.
	 *
	 * @param templateDb template found in database.
	 *
	 * @param template template with new values.
	 *
	 * @return database template with new values.
	 */
	private Subject updateSubjectValues(final Subject subjectDb, final SubjectFromShupDTO subjectFromShupDTO) {

		subjectDb.setName(subjectFromShupDTO.getName());
		subjectDb.setBirthDate(subjectFromShupDTO.getBirthDate());
		subjectDb.setIdentifier(subjectFromShupDTO.getIdentifier());
		subjectDb.setPseudonymusHashValues(subjectFromShupDTO.getPseudonymusHashValues());
		subjectDb.setSex(Sex.getSex(subjectFromShupDTO.getSex()));
		boolean foundStudy = false;
		for (SubjectStudy ss : subjectDb.getSubjectStudyList()) {
			if (ss.getStudy().getId() == subjectFromShupDTO.getStudyId()) {
				LOG.error("<BEFORE> Suject id " + ss.getId());
				LOG.error("<BEFORE> Suject/study id " + ss.getSubjectStudyIdentifier());
				LOG.error("<BEFORE> Suject Type " + ss.getSubjectType().name());
				LOG.error("<BEFORE> Suject physically involved " + ss.isPhysicallyInvolved());
				ss.setSubjectType(SubjectType.getType(subjectFromShupDTO.getSubjectType()));
				ss.setPhysicallyInvolved(subjectFromShupDTO.getPhysicallyInvolved());
				foundStudy = true;
			}
		}
		if (!foundStudy) {
			SubjectStudy subjectStudy =  new SubjectStudy();
			subjectStudy.setSubjectType(SubjectType.getType(subjectFromShupDTO.getSubjectType()));
			subjectStudy.setPhysicallyInvolved(subjectFromShupDTO.getPhysicallyInvolved());
			subjectStudy.setStudy(studyService.findById(subjectFromShupDTO.getStudyId()));
			subjectStudy.setSubject(subjectDb);
			if (subjectDb.getSubjectStudyList() == null) {
				List<SubjectStudy> subjectStudyList = new ArrayList<SubjectStudy>();
				subjectStudyList.add(subjectStudy);
				subjectDb.setSubjectStudyList(subjectStudyList);
			} else {
				subjectDb.getSubjectStudyList().add(subjectStudy);
			}
		}

		subjectDb.setManualHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getManualHemisphericDominance()));
		subjectDb.setLanguageHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getLanguageHemisphericDominance()));
		subjectDb.setImagedObjectCategory(ImagedObjectCategory.getCategory(subjectFromShupDTO.getImagedObjectCategory()));
		//subjectDb.setUserPersonalCommentList(subjectFromShupDTO.getUserPersonalCommentList());
		return subjectDb;
	}

	@Override
	public List<SimpleSubjectDTO> findAllSubjectsOfStudy(final Long studyId) {
		List<SimpleSubjectDTO> simpleSubjectDTOList = new ArrayList<SimpleSubjectDTO>();
		List<SubjectStudy> opt = subjectStudyRepository.findByStudy(studyRepository.findOne(studyId));
		if (opt != null) {
			for (SubjectStudy rel : opt) {
				SimpleSubjectDTO simpleSubjectDTO = new SimpleSubjectDTO();
				if (rel.getStudy().getId() == studyId) {
					Subject sub = rel.getSubject();
					simpleSubjectDTO.setId(sub.getId());
					simpleSubjectDTO.setName(sub.getName());
					simpleSubjectDTO.setIdentifier(sub.getIdentifier());
					simpleSubjectDTO.setSubjectStudy(subjectStudyMapper.subjectStudyToSubjectStudyDTO(rel));
					simpleSubjectDTOList.add(simpleSubjectDTO);
				}
			}
			return simpleSubjectDTOList;
		} else {
			LOG.info("No created subjects for study " + studyId);
			return null;
		}
	}

	@Override
	public Subject findByIdentifier(String identifier) {
		Optional<Subject> opt = subjectRepository.findByIdentifier(identifier);
		if (opt.isPresent())
			return opt.get();
		else {
			LOG.info("No existing subjects for identifier " + identifier);
			return null;
		}
	}

	public String createOfsepCommonName(Long studyCardId) {
		String commonName = "";
		Long idCenter = null;
		try {
			idCenter = getCenterIdFromStudyCard(studyCardId);
		} catch (ShanoirStudiesException e1) {
			return null;
		}

		DecimalFormat formatterCenter = new DecimalFormat("000");
		String commonNameCenter = formatterCenter.format(idCenter);

		String subjectOfsepCommonNameMaxFoundByCenter = findSubjectOfsepByCenter(commonNameCenter);
		int maxCommonNameNumber = 0;
		try {
			if (subjectOfsepCommonNameMaxFoundByCenter != null) {
				String maxNameToIncrement = subjectOfsepCommonNameMaxFoundByCenter.substring(3);
				maxCommonNameNumber = Integer.parseInt(maxNameToIncrement);
			}
			maxCommonNameNumber += 1;
			DecimalFormat formatterSubject = new DecimalFormat("0000");
			commonName = commonNameCenter + formatterSubject.format(maxCommonNameNumber);

		} catch (NumberFormatException e) {
			LOG.error("Th common name found contains non numeric characters : " + e.getMessage());

		}
		return commonName;
	}

	private Long getCenterIdFromStudyCard(Long studyCardId) throws ShanoirStudiesException {

		HttpEntity<Long> entity = null;
		try {
			entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
		} catch (ShanoirException e) {
			throw ((ShanoirStudiesException) e);
		}
		// Request to studycard MS to get center id
		ResponseEntity<Long> centerIdResponse = null;
		try {
			centerIdResponse = restTemplate.exchange(microservicesRequestsService.getStudycardsMsUrl()
					+ MicroserviceRequestsService.CENTERID + "/" + studyCardId, HttpMethod.GET, entity, Long.class);
		} catch (RestClientException e) {
			LOG.error("Error on study card microservice request", e);
			throw new ShanoirStudiesException("Error while getting study card list", StudiesErrorModelCode.SC_MS_COMM_FAILURE);
		}

		Long centerId = null;
		if (HttpStatus.OK.equals(centerIdResponse.getStatusCode())
				|| HttpStatus.NO_CONTENT.equals(centerIdResponse.getStatusCode())) {
			centerId = centerIdResponse.getBody();
		} else {
			throw new ShanoirStudiesException(StudiesErrorModelCode.SC_MS_COMM_FAILURE);
		}

		return centerId;
	}

	/**
	 * Browse through all subject ofsep using the center code (3 digital
	 * numbers).
	 *
	 * @param centerCode
	 *            the center code
	 *
	 * @return the string max of the subject common name
	 */
	@Override
	public String findSubjectOfsepByCenter(final String centerCode) {

		if (centerCode == null || "".equals(centerCode)) {
			return null;
		}
		String name = subjectRepository.find(centerCode);
		return name;
	}

	@Override
	public List<ExaminationDTO> findExaminationsForSubjectStudyRel(Long subjectId, Long studyId) throws ShanoirStudiesException {
		ResponseEntity<List<ExaminationDTO>> examinationsResponse = null;
		try {
			HttpEntity<Long> entity = null;
			try {
				entity = new HttpEntity<>(KeycloakUtil.getKeycloakHeader());
			} catch (ShanoirException e) {
				throw ((ShanoirStudiesException) e);
			}
			examinationsResponse = restTemplate.exchange(
					microservicesRequestsService.getExaminationMsUrl() + MicroserviceRequestsService.SUBJECT + subjectId +
					MicroserviceRequestsService.STUDY + studyId,
					HttpMethod.GET, entity, new ParameterizedTypeReference<List<ExaminationDTO>>() {
					});
		} catch (RestClientException e) {
			LOG.error("Error on examination microservice request", e);
			throw new ShanoirStudiesException("Error while getting study card list", StudiesErrorModelCode.SC_MS_COMM_FAILURE);
		}
		
		List<ExaminationDTO> examinations = null;
		if (HttpStatus.OK.equals(examinationsResponse.getStatusCode())
				|| HttpStatus.NO_CONTENT.equals(examinationsResponse.getStatusCode())) {
			examinations = examinationsResponse.getBody();
		} else {
			throw new ShanoirStudiesException(StudiesErrorModelCode.SC_MS_COMM_FAILURE);
		}
		
		return examinations;
	}

	@Override
	public Subject saveForOFSEP(SubjectFromShupDTO subjectFromShupDTO) throws ShanoirStudiesException {

		Subject subject = new Subject();
		subject.setName(subjectFromShupDTO.getName());
		subject.setBirthDate(subjectFromShupDTO.getBirthDate());
		subject.setIdentifier(subjectFromShupDTO.getIdentifier());
		subject.setImagedObjectCategory(ImagedObjectCategory.getCategory(subjectFromShupDTO.getImagedObjectCategory()));
		subject.setLanguageHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getLanguageHemisphericDominance()));
		subject.setManualHemisphericDominance(HemisphericDominance.getDominance(subjectFromShupDTO.getManualHemisphericDominance()));
		subject.setPseudonymusHashValues(subjectFromShupDTO.getPseudonymusHashValues());
		subject.setSex(Sex.getSex(subjectFromShupDTO.getSex()));
		
		SubjectStudy subjectStudy = new SubjectStudy();
		subjectStudy.setStudy(studyService.findById(subjectFromShupDTO.getStudyId()));
		subjectStudy.setPhysicallyInvolved(subjectFromShupDTO.getPhysicallyInvolved());
		subjectStudy.setSubject(subject);
		subjectStudy.setSubjectStudyIdentifier(subjectFromShupDTO.getSubjectStudyIdentifier());
		subjectStudy.setSubjectType(SubjectType.getType(subjectFromShupDTO.getSubjectType()));
		
		List<SubjectStudy> subjectStudyList = new ArrayList<SubjectStudy>();
		subjectStudyList.add(subjectStudy);
		subject.setSubjectStudyList(subjectStudyList);
	
		
		Subject savedSubject = null;
		String commonName = createOfsepCommonName(subjectFromShupDTO.getStudyCardId());
		if (commonName == null || commonName.equals(""))
			subject.setName("NoCommonName");
		else
			subject.setName(commonName);
		try {
			subjectStudy.setSubject(subject);
			savedSubject = subjectRepository.save(subject);
		} catch (DataIntegrityViolationException dive) {
			LOG.error("Error while creating subject", dive);
			throw new ShanoirStudiesException("Error while creating subject");
		}
		
		// updateShanoirOld(savedSubject);
		return savedSubject;
	}
	
	

}
