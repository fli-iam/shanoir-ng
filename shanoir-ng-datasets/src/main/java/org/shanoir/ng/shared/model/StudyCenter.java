package org.shanoir.ng.shared.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Simpler replication of study_center in MS Studies.
 * Updated by MS Studies via RabbitMQ. Used by Dicom-
 * ImporterService to avoid a RabbitMQ call to MS Studies
 * per DICOM image imported. Lookup in local database of
 * MS Datasets, instead of sending thousands of messages
 * for one single DICOM study. We need to know, if the
 * center for which we currently import is already in the
 * study, we are importing in.
 * 
 */
@Entity
@Table(name = "study_center")
public class StudyCenter {
    
    @Id
	private Long id;

    /** Center. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "center_id", nullable=false)
	private Center center;

	/** The study. */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "study_id", nullable=false)
	private Study study;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Center getCenter() {
        return center;
    }

    public void setCenter(Center center) {
        this.center = center;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

}
