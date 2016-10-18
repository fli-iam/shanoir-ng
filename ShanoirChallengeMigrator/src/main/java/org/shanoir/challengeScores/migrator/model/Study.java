package org.shanoir.challengeScores.migrator.model;

import java.util.List;

import org.shanoir.challengeScores.migrator.Utils;

/**
 * A metric is an evaluation method from the SegPerfAnalyser, to evaluate the segmentation quality.
 * This class holds a metric and its configuration, meaning what to do in some particular cases.
 *
 * @author jlouis
 */
public class Study {

	private Long id = null;

	private String name;

	private List<Metric> metrics;


	/**
	 * Constructor
	 */
	public Study() {

	}


	/**
	 * Constructor
	 *
	 * @param studyId
	 */
	public Study(Long studyId) {
		this.setId(studyId);
	}


	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Study) {
			Study other = (Study) obj;
			if (this.getId() == null) {
				return other.getId() == null && Utils.equals(this.getName(), other.getName());
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
			return name.hashCode();
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
	 * @return the metrics
	 */
	public List<Metric> getMetrics() {
		return metrics;
	}


	/**
	 * @param metrics the metrics to set
	 */
	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}
}
