package org.shanoir.studycard.service;

import java.util.List;

import org.shanoir.studycard.model.StudyCard;

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
     * @return containing the found study card.
     */
    StudyCard findById(long id);
    
    /**
     * Method looks for a study card by its name.
     *
     * @param name the name of a study card we are looking for.
     * @return containing the found study card.
     */
    StudyCard findByName(String name);
    
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
     * @param id study card id.
     * @param studyCard study card to update.
     * @return updated study card.
     */
    StudyCard update(Long id, StudyCard studyCard);
    
    /**
     * Deletes a study card by its id.
     * 
     * @param id study card id.
     */
    void deleteById(Long id);
    
}
