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

package org.shanoir.ng.studycard.model.condition;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.studycard.model.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Check(constraints = "(dicomTag IS NOT NULL AND shanoirField IS NULL) OR (dicomTag IS NULL AND shanoirField IS NOT NULL)") 
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name="scope", discriminatorType = DiscriminatorType.STRING)
public abstract class StudyCardCondition extends AbstractEntity {
	
	private static final Logger LOG = LoggerFactory.getLogger(StudyCardCondition.class);
	
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name="condition_id")
	private List<StudyCardConditionValue> values;
	
	@NotNull
	private int operation;

	public Operation getOperation() {
		return Operation.getType(operation);
	}

	public void setOperation(Operation operation) {
		this.operation = operation.getId();
	}

	public List<StudyCardConditionValue> getValues() {
		return values;
	}

	public void setValues(List<StudyCardConditionValue> values) {
		this.values = values;
	}
    
    protected boolean numericalCompare(Operation operation, int comparison) {
        if (Operation.BIGGER_THAN.equals(operation)) {
            return comparison > 0;
        } else if (Operation.EQUALS.equals(operation)) {
            return comparison == 0;
        } else if (Operation.SMALLER_THAN.equals(operation)) {
            return comparison < 0;
        }
        throw new IllegalArgumentException("Cannot use this method for non-numerical operations (" + operation + ")");
    }
    
    protected boolean textualCompare(Operation operation, String original, String studycardStr) {
        if (original != null) {
            if (Operation.EQUALS.equals(operation)) {
                return original.equals(studycardStr);
            } else if (Operation.CONTAINS.equals(operation)) {
                return original.contains(studycardStr);
            } else if (Operation.STARTS_WITH.equals(operation)) {
                return original.startsWith(studycardStr);
            } else if (Operation.ENDS_WITH.equals(operation)) {
                return original.endsWith(studycardStr);
            }
        } else {
            LOG.error("Error in studycard processing: tag (from pacs) or field (from database) null.");
            return false;
        }
        throw new IllegalArgumentException("Cannot use this method for non-textual operations (" + operation + ")");
    }

}
