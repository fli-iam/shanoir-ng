package org.shanoir.challengeScores.migrator.model;

import java.util.Arrays;
import java.util.List;

import org.shanoir.challengeScores.migrator.Utils;

/**
 * A metric is an evaluation method from the SegPerfAnalyser, to evaluate the segmentation quality.
 * This class holds a metric and its configuration, meaning what to do in some particular cases.
 *
 * @author jlouis
 */
public class Metric {

	private Long id = null;

	private String name;

	/** What to do if a value is NaN ? */
	private String NaN;

	/** What to do if a value is a positive infinite ? */
	private String posInf;

	/** What to do if a value is a negative infinite ? */
	private String negInf;

	/** The study ids for this metric. */
	private List<Study> studies;


	/**
	 * Constructor
	 */
	public Metric() {

	}


	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Metric) {
			Metric other = (Metric) obj;
			if (this.getId() == null) {
				return other.getId() == null
						&& Utils.equals(this.getName(), other.getName())
						&& Utils.equals(this.getNaN(), other.getNaN())
						&& Utils.equals(this.getNegInf(), other.getNegInf())
						&& Utils.equals(this.getPosInf(), other.getPosInf());
			} else {
				return this.getId().equals(other.getId());
			}
		} else {
			return false;
		}
	}


	@Override
	public int hashCode() {
		if (id != null) {
			return id.hashCode();
		} else {
			return Arrays.hashCode(new Object[] {name, NaN, negInf, posInf});
		}
	}


	@Override
	public String toString() {
		return name;
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
