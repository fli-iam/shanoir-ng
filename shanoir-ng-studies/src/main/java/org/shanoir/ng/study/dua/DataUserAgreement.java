package org.shanoir.ng.study.dua;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.shanoir.ng.shared.core.model.AbstractEntity;
import org.shanoir.ng.study.model.Study;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "data_user_agreement")
@GenericGenerator(name = "IdOrGenerate", strategy = "org.shanoir.ng.shared.model.UseIdOrGenerate")
public class DataUserAgreement extends AbstractEntity {

	private static final long serialVersionUID = 6095755233940273029L;

	@ManyToOne
	@NotNull
	@JsonProperty("studyId")
	@JsonIdentityReference(alwaysAsId = true)
	private Study study;

	@NotNull
	private Long userId;
	
	@CreationTimestamp
	@Column(updatable=false)
	private Date timestampOfNew;
	
	private Date timestampOfAccepted;

	public Study getStudy() {
		return study;
	}

	public Long getUserId() {
		return userId;
	}

	public Date getTimestampOfNew() {
		return timestampOfNew;
	}

	public Date getTimestampOfAccepted() {
		return timestampOfAccepted;
	}

	public void setStudy(Study study) {
		this.study = study;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public void setTimestampOfNew(Date timestampOfNew) {
		this.timestampOfNew = timestampOfNew;
	}

	public void setTimestampOfAccepted(Date timestampOfAccepted) {
		this.timestampOfAccepted = timestampOfAccepted;
	}
	
	@JsonProperty("path")
	public String getPath() {
		return this.study.getDataUserAgreementPaths().get(0);
	}

	@JsonProperty("studyName")
	public String getStudyName() {
		return this.study.getName();
	}

}
