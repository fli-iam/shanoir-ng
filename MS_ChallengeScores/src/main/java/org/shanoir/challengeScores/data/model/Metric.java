package org.shanoir.challengeScores.data.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedNativeQuery;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.GenericGenerator;
import org.shanoir.challengeScores.utils.Utils;

/**
 * A metric is an evaluation method from the SegPerfAnalyser, to evaluate the segmentation quality.
 * This class holds a metric and its configuration, meaning what to do in some particular cases.
 *
 * @author jlouis
 */
@Entity
@NamedNativeQuery(name="getLastId", query="SELECT MAX(ID) FROM METRIC")
public class Metric {

	@Id
	@NotNull
	@GeneratedValue(generator = "myGenerator")
	@GenericGenerator(name = "myGenerator", strategy = "org.shanoir.challengeScores.utils.MetricIdentifierGenerator")
	private Long id;

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
			joinColumns = @JoinColumn(name="METRIC_ID"),
			inverseJoinColumns = @JoinColumn(name="STUDY_ID"))
	private List<Study> studies;


	/**
	 * Constructor
	 */
	public Metric() {

	}


	public Metric(Long id2, String name2, String naN, String negInf2, String posInf2, List<Long> studyIds) {
		setName(name);
		setNaN(naN);
		setNegInf(negInf);
		setPosInf(posInf);
		List<Study> studies = new ArrayList<Study>();
		if (studyIds != null) {
			for (Long studyId : studyIds) {
				Study study = new Study(studyId);
				studies.add(study);
			}
		}
		setStudies(studies);
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
