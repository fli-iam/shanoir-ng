package org.shanoir.ng.studycard.model.condition;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;

@Entity
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class StudyCardConditionValue  extends AbstractEntity {

	/** UID */
	private static final long serialVersionUID = 6703377853115591193L;

	@NotNull
	private String value;
	
	public StudyCardConditionValue() {
	}
	
	public StudyCardConditionValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
