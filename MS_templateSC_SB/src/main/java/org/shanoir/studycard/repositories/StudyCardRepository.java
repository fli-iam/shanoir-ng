package org.shanoir.studycard.repositories;

import org.shanoir.studycard.model.StudyCard;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for study cards.
 * 
 * @author msimon
 *
 */
public interface StudyCardRepository extends CrudRepository<StudyCard, Long> {

    /**
     * Method looks for a study card by its id.
     *
     * @param id the id of a study card we are looking for.
     * @return containing the found study card.
     */
    StudyCard findById(long id);
    
    /**
     * Method looks for a study card by its name.
     * Name is unique so return first one.
     *
     * @param name the name of a study card we are looking for.
     * @return containing the found study card.
     */
    StudyCard findFirstByName(String name);
    
}
