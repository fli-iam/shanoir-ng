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

package org.shanoir.ng.studycard.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.shanoir.ng.shared.model.Study;
import org.shanoir.ng.shared.model.SubjectStudy;
import org.shanoir.ng.shared.quality.QualityTag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This class contains the result of an application of a study card
 * on an entire study. For each examination, when the result is wrong
 * on the examination level already, we will not check deeper on the
 * acquisition level. When everything is clear on the examination level,
 * we check deeper on the acquisition level, if there is an error, we stop.
 * Same for the dataset level.
 * 
 * In the idea, that it does not make sense to display an error on a dataset,
 * when there is already a sequence missing on the exam. Display, that at first
 * and then display the dataset result, when that problem solved?
 * 
 * @author mkain
 *
 */
public class QualityCardResult extends CopyOnWriteArrayList<QualityCardResultEntry> {
    
    private List<SubjectStudy> updatedSubjectStudies = new CopyOnWriteArrayList<>();
    
    public List<SubjectStudy> getUpdatedSubjectStudies() {
        return updatedSubjectStudies;
    }

    private void setUpdatedSubjectStudies(List<SubjectStudy> updatedSubjectStudies) {
        this.updatedSubjectStudies = updatedSubjectStudies;
    }
    
    public void addUpdatedSubjectStudy(SubjectStudy subjectStudy) {
        if (getUpdatedSubjectStudies() == null) setUpdatedSubjectStudies(new ArrayList<>());
        if (subjectStudy == null || subjectStudy.getId() == null) return;
        for (SubjectStudy presentSubStu : getUpdatedSubjectStudies()) {
            if (subjectStudy.getId().equals(presentSubStu.getId()) 
                    && presentSubStu.getQualityTag().getId() >= subjectStudy.getQualityTag().getId()) {
                return;
            }
        }
        getUpdatedSubjectStudies().add(subjectStudy);
    }

    /***
     * Remove unchanged subject-studies 
     * @param study the study containing the original subject-studies
     */
    public void removeUnchanged(Study study) {
        if (getUpdatedSubjectStudies() == null) return;
        for (SubjectStudy original : study.getSubjectStudyList()) {
            getUpdatedSubjectStudies().removeIf(updated -> 
                updated.getId().equals(original.getId()) 
                && updated.getQualityTag() != null
                && updated.getQualityTag().equals(original.getQualityTag())
             );
        }
    }

    public void merge(QualityCardResult result) {
        this.addAll(result);
        if (result.getUpdatedSubjectStudies() != null) {
            for (SubjectStudy subjectStudy : result.getUpdatedSubjectStudies()) {
                this.addUpdatedSubjectStudy(subjectStudy);
            }
        }
    }

    public boolean hasError() {
        for (QualityCardResultEntry entry : this) {
            if (QualityTag.ERROR.equals(entry.getTagSet())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasWarning() {
        for (QualityCardResultEntry entry : this) {
            if (QualityTag.WARNING.equals(entry.getTagSet())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFailedValid() {
        for (QualityCardResultEntry entry : this) {
            if (entry.isFailedValid()) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "json error";
        }
    }
    
}
