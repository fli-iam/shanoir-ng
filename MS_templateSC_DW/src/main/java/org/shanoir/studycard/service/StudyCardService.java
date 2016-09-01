package org.shanoir.studycard.service;

import java.util.List;

import org.shanoir.studycard.model.StudyCard;

import com.google.common.base.Optional;

/**
 * Service interface for study cards.
 * 
 * @author msimon
 *
 */
public interface StudyCardService {

    /**
     * Method returns all study cards stored in the database.
     *
     * @return list of all study cards stored in the database
     */
    List<StudyCard> findAll();

    /**
     * Method looks for a study card by its id.
     *
     * @param id the id of a study card we are looking for.
     * @return Optional containing the found study card or an empty Optional otherwise.
     */
    Optional<StudyCard> findById(long id);
    
    /**
     * Saves a study card.
     * 
     * @param studyCard study card to save.
     * @return saved study card.
     */
    StudyCard save(StudyCard studyCard);
    
    /**
     * Updates a study card.
     * 
     * @param studyCard study card to update.
     * @return updated study card.
     */
    StudyCard update(StudyCard studyCard);
    
    /**
     * Deletes a study card.
     * 
     * @param long id the id of the study card to delete.
     */
    void delete(long id);
    
}
