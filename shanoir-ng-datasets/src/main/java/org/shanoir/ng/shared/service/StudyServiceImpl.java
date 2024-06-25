/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.shared.service;

import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.Tag;
import org.shanoir.ng.tag.repository.StudyTagRepository;
import org.shanoir.ng.vip.resulthandler.ResultHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Component
public class StudyServiceImpl implements StudyService {

	@Autowired
	private StudyRepository repository;

	@Autowired
	private DatasetRepository dsRepository;
	
	@Override
	public Study findById(final Long id) {
		return repository.findById(id).orElse(null);
	}

	@Transactional
	public void updateStudy(Study updated, Study current) {

		if (current.getId() == null)
			throw new IllegalStateException("The entity should have an id.");

		// TAGS
		if (current.getTags() != null) {
			current.getTags().clear();
		} else {
			current.setTags(new ArrayList<>());
		}
		if (updated.getTags() != null) {
			current.getTags().addAll(updated.getTags());
		}
		for (Tag tag : current.getTags()) {
			tag.setStudy(current);
		}

		// STUDY TAGS
		if (current.getStudyTags() != null) {
			current.getStudyTags().clear();
		} else {
			current.setTags(new ArrayList<>());
		}
		if (updated.getStudyTags() != null) {
			current.getStudyTags().addAll(updated.getStudyTags());
		}
		for (StudyTag tag : current.getStudyTags()) {
			tag.setStudy(current);
		}

		Study studyDb = this.repository.save(current);

		// SUBJECT_STUDY
		if (current.getSubjectStudyList() != null) {
			current.getSubjectStudyList().clear();
		} else {
			current.setSubjectStudyList(new ArrayList<>());
		}
		if (updated.getSubjectStudyList() != null) {
			current.getSubjectStudyList().addAll(updated.getSubjectStudyList());
		}

		for (SubjectStudy sustu : current.getSubjectStudyList()) {
			sustu.setStudy(current);
			for (Tag tag : sustu.getTags()) {
				if (tag.getId() == null) {
					Tag dbTag = studyDb.getTags().stream().filter(upTag ->
							upTag.getColor().equals(tag.getColor()) && upTag.getName().equals(tag.getName())
					).findFirst().orElse(null);
					if (dbTag != null) {
						tag.setId(dbTag.getId());
					} else {
						throw new IllegalStateException("Cannot link a new tag to a subject-study, this tag does not exist in the study");
					}
				}
			}
		}
		this.repository.save(current);
	}

	@Override
	public List<String> validate(Study updated, Study current){

		List<String> errors = new ArrayList<>();

		for(StudyTag tag : current.getStudyTags()){
            if (!updated.getStudyTags().contains(tag)
					&& this.dsRepository.existsByTagsContains(tag)) {
                errors.add("Study tag [" + tag.getName() + "] can't be removed because it's linked to at least one dataset.");
            }
		}

		return errors;
	}
}
