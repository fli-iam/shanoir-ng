package org.shanoir.ng.subject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.shanoir.ng.configuration.amqp.RabbitMqConfiguration;
import org.shanoir.ng.shared.exception.ShanoirSubjectException;
import org.shanoir.ng.study.StudyRepository;
import org.shanoir.ng.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

	private Object rel;

	@Override
	public void deleteById(final Long id) throws ShanoirSubjectException {
		subjectRepository.delete(id);
	}

	@Override
	public List<Subject> findAll() {
		return Utils.toList(subjectRepository.findAll());
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
	public Subject save(final Subject subject) throws ShanoirSubjectException {
		Subject savedSubject = null;
		try {
			savedSubject = subjectRepository.save(subject);
		} catch (DataIntegrityViolationException dive) {
			ShanoirSubjectException.logAndThrow(LOG, "Error while creating Subject: " + dive.getMessage());
		}
		updateShanoirOld(savedSubject);
		return savedSubject;
	}
	
	@Override
	public Subject saveFromJson(final File jsonFile) throws ShanoirSubjectException {
		
		ObjectMapper mapper = new ObjectMapper();
		Subject subject=new Subject();
		try {
			subject = mapper.readValue(jsonFile, Subject.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		Subject savedSubject = null;
		try {
			savedSubject = subjectRepository.save(subject);
		} catch (DataIntegrityViolationException dive) {
			ShanoirSubjectException.logAndThrow(LOG, "Error while creating Subject: " + dive.getMessage());
		}
		updateShanoirOld(savedSubject);
		return savedSubject;
	}

	@Override
	public Subject update(final Subject subject) throws ShanoirSubjectException {
		final Subject subjectDb = subjectRepository.findOne(subject.getId());
		updateSubjectValues(subjectDb, subject);
		try {
			subjectRepository.save(subjectDb);
		} catch (Exception e) {
			ShanoirSubjectException.logAndThrow(LOG, "Error while updating Subject: " + e.getMessage());
		}
		updateShanoirOld(subjectDb);
		return subjectDb;
	}

	@Override
	public void updateFromShanoirOld(final Subject subject) throws ShanoirSubjectException {
		if (subject.getId() == null) {
			throw new IllegalArgumentException("Subject id cannot be null");
		} else {
			final Subject subjectDb = subjectRepository.findOne(subject.getId());
			if (subjectDb != null) {
				try {
					subjectDb.setName(subject.getName());
					subjectRepository.save(subjectDb);
				} catch (Exception e) {
					ShanoirSubjectException.logAndThrow(LOG,
							"Error while updating Subject from Shanoir Old: " + e.getMessage());
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
	private boolean updateShanoirOld(final Subject subject) {
		try {
			LOG.info("Send update to Shanoir Old");
			rabbitTemplate.convertAndSend(RabbitMqConfiguration.queueOut().getName(),
					new ObjectMapper().writeValueAsString(subject));
			return true;
		} catch (AmqpException e) {
			LOG.error("Cannot send Subject " + subject.getId() + " save/update to Shanoir Old on queue : "
					+ RabbitMqConfiguration.queueOut().getName(), e);
		} catch (JsonProcessingException e) {
			LOG.error("Cannot send Subject " + subject.getId() + " save/update because of an error while serializing Subject.",
					e);
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
		subjectDb.setBirthDate(subject.getBirthDate());
		subjectDb.setIdentifier(subject.getIdentifier());
		subjectDb.setPseudonymusHashValues(subject.getPseudonymusHashValues());
		subjectDb.setSex(subject.getSex());
		subjectDb.setSubjectStudyList(subject.getSubjectStudyList());
		return subjectDb;
	}
	
	@Override
	public List<Subject> findAllSubjectsOfStudy(final Long studyId) {
		List<Subject> listSubjects=new ArrayList<Subject>();
		Optional<List<SubjectStudy>> opt=subjectStudyRepository.findByStudy(studyRepository.findOne(studyId));
		if (opt.isPresent()) {
			List<SubjectStudy> relList = opt.get();
			for (int i = 0; i < relList.size(); i++){
				SubjectStudy rel = relList.get(i);
		        Subject sub=rel.getSubject();
		        listSubjects.add(sub);
		        
		}
			return listSubjects;
		}
		else {
		    LOG.info("No created subjects for study " + studyId);
		    return null;
		}
	}

	@Override
	public Subject findByIdentifier(String identifier) {
		Optional<Subject> opt=subjectRepository.findByIdentifier(identifier);
		if (opt.isPresent())
		 return opt.get();
		else
		{
			LOG.info("No existing subjects for identifier " + identifier);
		    return null;
		}
	}
	
	

}

