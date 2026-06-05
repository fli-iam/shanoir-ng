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

package org.shanoir.ng.studycard.model.rule;

import java.util.List;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.shared.quality.QualityTag;
import org.shanoir.ng.studycard.model.condition.StudyCardCondition;
import org.shanoir.ng.studycard.model.condition.StudyCardDICOMConditionOnDatasets;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;

@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class QualityExaminationRule extends AbstractEntity {

    private Integer tag;

    @NotNull
    private boolean orConditions;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    // there is a join table because a rule_id fk would lead to an ambiguity and bugs
    // because it could refer to a study card or quality card rule
    @JoinTable(name = "quality_card_condition_join", joinColumns = {@JoinColumn(name = "quality_card_rule_id")}, inverseJoinColumns = {@JoinColumn(name = "condition_id")})
    private List<StudyCardCondition> conditions;

    public QualityTag getQualityTag() {
        return QualityTag.get(tag);
    }

    public void setQualityTag(QualityTag tag) {
        this.tag = tag != null ? tag.getId() : null;
    }

    public List<StudyCardCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<StudyCardCondition> conditions) {
        this.conditions = conditions;
    }

    public boolean isOrConditions() {
        return orConditions;
    }

    public void setOrConditions(boolean orConditions) {
        this.orConditions = orConditions;
    }

    public boolean hasDicomConditions() {
        if (getConditions() != null) {
            for (StudyCardCondition condition: getConditions()) {
                if (condition instanceof StudyCardDICOMConditionOnDatasets) {
                    return true;
                }
            }
        }
        return false;
    }
}
