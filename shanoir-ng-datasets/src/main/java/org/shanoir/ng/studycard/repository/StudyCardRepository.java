package org.shanoir.ng.studycard.repository;

import java.util.List;

import org.shanoir.ng.studycard.model.StudyCard;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for study cards.
 *
 * @author msimon
 */
public interface StudyCardRepository extends CrudRepository<StudyCard, Long> {

	/**
	 * Find list of study card by their study id.
	 * 
	 * @param studyIdList list of study ids.
	 * @return list of study cards.
	 */
	List<StudyCard> findByStudyIdIn(List<Long> studyIdList);

}
