package org.shanoir.studycard.service.impl;

import java.util.List;

import org.shanoir.studycard.core.StudyCardDAO;
import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.service.StudyCardService;

import com.google.common.base.Optional;
import com.google.inject.Inject;

/**
 * Service for study cards.
 * 
 * @author msimon
 *
 */
public class StudyCardServiceImpl implements StudyCardService {

	private StudyCardDAO studyCardDAO;

	/**
	 * @param studyCardDAO the studyCardDAO to set
	 */
	@Inject
	public void setStudyCardDAO(StudyCardDAO studyCardDAO) {
		this.studyCardDAO = studyCardDAO;
	}
	
	/* (non-Javadoc)
	 * @see org.shanoir.studycard.service.StudyCardService#findAll()
	 */
	public List<StudyCard> findAll() {
		return studyCardDAO.findAll();
	}

	/* (non-Javadoc)
	 * @see org.shanoir.studycard.service.StudyCardService#findById(long)
	 */
	public Optional<StudyCard> findById(long id) {
		return studyCardDAO.findById(id);
	}

	/* (non-Javadoc)
	 * @see org.shanoir.studycard.service.StudyCardService#save(org.shanoir.studycard.model.StudyCard)
	 */
	public StudyCard save(StudyCard studyCard) {
		return studyCardDAO.save(studyCard);
	}

	/* (non-Javadoc)
	 * @see org.shanoir.studycard.service.StudyCardService#update(org.shanoir.studycard.model.StudyCard)
	 */
	public StudyCard update(StudyCard studyCard) {
		return studyCardDAO.update(studyCard);
	}

	@Override
	public void delete(long id) {
		studyCardDAO.delete(id);
	}

}
