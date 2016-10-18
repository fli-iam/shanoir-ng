package org.shanoir.challengeScores.migrator.model;

import org.shanoir.challengeScores.migrator.Utils;

public class Score {

	private Long id = null;

	private Float value;

	private Metric metric;

	private Challenger owner;

	private Patient patient;

	private Study study;

	/** Id of the main dataset used as an input for the segmentation. */
	private Long inputDatasetId;


	public Score() {

	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getCell(String.valueOf(id), 10));
		sb.append(getCell(String.valueOf(value), 15));
		sb.append(getCell(study.getName(), 30));
		sb.append(getCell(metric.getName(), 25));
		sb.append(getCell(owner.getName(), 20));
		sb.append(getCell(patient.getName(), 20));
		sb.append(getCell(String.valueOf(inputDatasetId), 10));
		sb.append("|");
		return sb.toString();
	}


	private String getCell(String str, int width) {
		return new StringBuilder().append("| ").append(str).append(Utils.repeat(" ", width - str.length())).toString();
	}


	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the value
	 */
	public Float getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Float value) {
		this.value = value;
	}

	/**
	 * @return the metric
	 */
	public Metric getMetric() {
		return metric;
	}

	/**
	 * @param metric the metric to set
	 */
	public void setMetric(Metric metric) {
		this.metric = metric;
	}

	/**
	 * @return the owner
	 */
	public Challenger getOwner() {
		return owner;
	}

	/**
	 * @param owner the owner to set
	 */
	public void setOwner(Challenger owner) {
		this.owner = owner;
	}

	/**
	 * @return the patient
	 */
	public Patient getPatient() {
		return patient;
	}

	/**
	 * @param patient the patient to set
	 */
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	/**
	 * @return the inputDatasetId
	 */
	public Long getInputDatasetId() {
		return inputDatasetId;
	}

	/**
	 * @param inputDatasetId the inputDatasetId to set
	 */
	public void setInputDatasetId(Long inputDatasetId) {
		this.inputDatasetId = inputDatasetId;
	}

	/**
	 * @return the study
	 */
	public Study getStudy() {
		return study;
	}

	/**
	 * @param study the study to set
	 */
	public void setStudy(Study study) {
		this.study = study;
	}

}
