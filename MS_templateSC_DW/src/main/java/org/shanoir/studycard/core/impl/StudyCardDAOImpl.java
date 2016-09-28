package org.shanoir.studycard.core.impl;

import java.util.List;

import org.hibernate.SessionFactory;
import org.shanoir.studycard.core.StudyCardDAO;
import org.shanoir.studycard.model.StudyCard;

import com.google.common.base.Optional;
import com.google.inject.Inject;

import io.dropwizard.hibernate.AbstractDAO;

/**
 * DAO for study cards.
 * 
 * @author msimon
 *
 */
public class StudyCardDAOImpl extends AbstractDAO<StudyCard> implements StudyCardDAO {

	@Inject
	public StudyCardDAOImpl(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

    public List<StudyCard> findAll() {
        return list(namedQuery("org.shanoir.studycard.model.StudyCard.findAll"));
    }

    public Optional<StudyCard> findById(long id) {
        return Optional.fromNullable(get(id));
    }
    
    public StudyCard save(StudyCard studyCard) {
    	return persist(studyCard);
    }
    
    public StudyCard update(StudyCard studyCard) {
    	return persist(studyCard);
    }

	@Override
	public void delete(long id) {
		final StudyCard studyCard = get(id);
		if (studyCard != null) {
			currentSession().delete(studyCard);
		}
	}
    
}
