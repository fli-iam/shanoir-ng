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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.shanoir.ng.dataset.repository.DatasetRepository;
import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.repository.StudyRepository;
import org.shanoir.ng.tag.model.StudyTag;
import org.shanoir.ng.tag.model.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


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
			throw new IllegalStateException("The entity must have an ID.");

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
			current.setStudyTags(new HashSet<>());
		}
		if (updated.getStudyTags() != null) {
			current.getStudyTags().addAll(updated.getStudyTags());
		}
		for (StudyTag tag : current.getStudyTags()) {
			tag.setStudy(current);
		}

		this.repository.save(current);
	}

	@Override
	public List<String> validate(Study updated, Study current) {

		List<String> errors = new ArrayList<>();

		if (current.getStudyTags() == null) {
			return errors;
		}

		for (StudyTag tag : current.getStudyTags()) {
            if (!updated.getStudyTags().contains(tag)
					&& this.dsRepository.existsByTagsContains(tag)) {
                errors.add("Study tag [" + tag.getName() + "] can't be removed because it's linked to at least one dataset.");
            }
		}

		return errors;
	}

}
