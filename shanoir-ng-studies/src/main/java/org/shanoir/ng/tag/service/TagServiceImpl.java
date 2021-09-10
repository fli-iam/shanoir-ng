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
package org.shanoir.ng.tag;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.subject.model.Subject;
import org.shanoir.ng.subject.service.SubjectServiceImpl;
import org.shanoir.ng.subjectstudy.model.SubjectStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class TagServiceImpl implements TagService {
	
	@Autowired
	SubjectServiceImpl subjectService;

	@Override
	public Map<Long, List<Long>> getSubjectTag(List<String> subjectNames) {
		List<Subject> subjects = Collections.emptyList();//subjectService.findByNames(subjectNames);
		if (CollectionUtils.isEmpty(subjects)) {
			return Collections.emptyMap();
		}
		Map<Long, List<Long>> tags = new HashMap<>();
		for (Subject sub : subjects) {
			if (CollectionUtils.isEmpty(sub.getSubjectStudyList())) {
				tags.put(sub.getId(), Collections.emptyList());
				continue;
			}
			List<Long> tagsPerSub = new ArrayList<>();
			for (SubjectStudy ss : sub.getSubjectStudyList()) {
				if (CollectionUtils.isEmpty(ss.getTags())) {
					continue;
				}
				tagsPerSub.addAll(ss.getTags().stream().map(Tag::getId).collect(toList()));
			}
			tags.put(sub.getId(), tagsPerSub);
		}
		return tags;
	}

}
