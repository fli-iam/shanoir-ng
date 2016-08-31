package org.shanoir.studycard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.studycard.model.StudyCard;
import org.shanoir.studycard.repositories.StudyCardRepository;
import org.shanoir.studycard.service.StudyCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for study cards.
 * 
 * @author msimon
 *
 */
@Service
public class StudyCardServiceImpl implements StudyCardService {

	@Autowired
	private StudyCardRepository studyCardRepository;

	@Override
	public List<StudyCard> findAll() {
		Iterable<StudyCard> dbResults = studyCardRepository.findAll();
		List<StudyCard> results = new ArrayList<StudyCard>();
		
		for (StudyCard studyCard : dbResults) {
			results.add(studyCard);
		}
		
		return results;
	}

	@Override
	public StudyCard findById(long id) {
		return studyCardRepository.findById(id);
	}

	@Override
	public StudyCard findByName(String name) {
		return studyCardRepository.findFirstByName(name);
	}

	@Override
	public StudyCard save(StudyCard studyCard) {
		return studyCardRepository.save(studyCard);
	}

	@Override
	public StudyCard update(Long id, StudyCard studyCard) {
		StudyCard dbStudyCard = studyCardRepository.findOne(id);
		if (dbStudyCard == null) {
			return null;
		}
		dbStudyCard.setAcquisitionEquipmentId(studyCard.getAcquisitionEquipmentId());
		dbStudyCard.setCenterId(studyCard.getCenterId());
		dbStudyCard.setDisabled(studyCard.isDisabled());
		dbStudyCard.setName(studyCard.getName());
		dbStudyCard.setNiftiConverter(studyCard.getNiftiConverter());
		dbStudyCard.setStudyId(studyCard.getStudyId());
        return studyCardRepository.save(dbStudyCard);
	}

	@Override
	public void deleteById(Long id) {
		studyCardRepository.delete(id);
	}

}
