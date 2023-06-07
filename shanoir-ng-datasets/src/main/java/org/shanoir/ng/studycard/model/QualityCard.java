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

package org.shanoir.ng.studycard.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.validation.constraints.NotNull;

import org.dcm4che3.data.Attributes;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.NotBlank;
import org.shanoir.ng.examination.model.Examination;
import org.shanoir.ng.shared.hateoas.HalEntity;
import org.shanoir.ng.shared.hateoas.Links;
import org.shanoir.ng.shared.validation.Unique;
import org.shanoir.ng.studycard.dto.QualityCardResult;
import org.shanoir.ng.studycard.model.rule.QualityExaminationRule;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Study card.
 *
 * @author msimon
 *
 */
@SuppressWarnings("deprecation")
@Entity
@JsonPropertyOrder({ "_links", "id", "name", "isDisabled" })
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class QualityCard extends HalEntity implements Card {

	/** The name of the study card. */
    @NotBlank
	@Column(unique = true)
	@Unique
	private String name;

	/** The study for which is defined the study card. */
	private Long studyId;
	
	@NotNull
	private boolean toCheckAtImport;
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name="quality_card_id")
	private List<QualityExaminationRule> rules;

	/**
	 * Init HATEOAS links
	 */
	@PostLoad
	public void initLinks() {
		this.addLink(Links.REL_SELF, "studycard/" + getId());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getStudyId() {
		return studyId;
	}

	public void setStudyId(Long studyId) {
		this.studyId = studyId;
	}

	public List<QualityExaminationRule> getRules() {
		return rules;
	}

	public void setRules(List<QualityExaminationRule> rules) {
		this.rules = rules;
	}
	
	/**
    * Application during import, when dicoms are present in tmp directory.
    * @param examination
    * @param studyCard
    * @param dicomAttributes
    */
    public QualityCardResult apply(Examination examination, Attributes dicomAttributes) {
        QualityCardResult result = new QualityCardResult();
        if (this.getRules() != null) {
            for (QualityExaminationRule rule : this.getRules()) {
                rule.apply(examination, dicomAttributes, result);
            }
        }
        return result;
    }

    public boolean isToCheckAtImport() {
        return toCheckAtImport;
    }

    public void setToCheckAtImport(boolean toCheckAtImport) {
        this.toCheckAtImport = toCheckAtImport;
    }
}
