package org.shanoir.challengeScores.data.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;

/**
 * A metric is an evaluation method from the SegPerfAnalyser, to evaluate the segmentation quality.
 * This class holds a metric and its configuration, meaning what to do in some particular cases.
 *
 * @author jlouis
 */
@Entity
public class Metric {

	@Id
	@GeneratedValue
	private long id;

	private String name;

	/** What to do if a value is NaN ? */
	private String NaN;

	/** What to do if a value is a positive infinite ? */
	private String posInf;

	/** What to do if a value is a negative infinite ? */
	private String negInf;

	/** The study ids for this metric. */
	@ManyToMany @JoinTable (
			name = "METRIC_STUDY_REL",
			joinColumns = @JoinColumn(name="STUDY_ID"),
			inverseJoinColumns = @JoinColumn(name="METRIC_ID"))
	private List<Study> studies;


	/**
	 * Constructor
	 */
	public Metric() {

	}


	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the naN
	 */
	public String getNaN() {
		return NaN;
	}


	/**
	 * @param naN the naN to set
	 */
	public void setNaN(String naN) {
		NaN = naN;
	}


	/**
	 * @return the posInf
	 */
	public String getPosInf() {
		return posInf;
	}


	/**
	 * @param posInf the posInf to set
	 */
	public void setPosInf(String posInf) {
		this.posInf = posInf;
	}


	/**
	 * @return the negInf
	 */
	public String getNegInf() {
		return negInf;
	}


	/**
	 * @param negInf the negInf to set
	 */
	public void setNegInf(String negInf) {
		this.negInf = negInf;
	}


	/**
	 * @return the studies
	 */
	public List<Study> getStudies() {
		return studies;
	}


	/**
	 * @param studies the studies to set
	 */
	public void setStudies(List<Study> studies) {
		this.studies = studies;
	}


}
