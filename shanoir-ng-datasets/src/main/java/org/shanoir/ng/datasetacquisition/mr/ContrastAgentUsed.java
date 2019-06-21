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

package org.shanoir.ng.datasetacquisition.mr;

/**
 * Contrast agent used.
 * 
 * @author msimon
 *
 */
public enum ContrastAgentUsed {

	// GADOLINIUM
	GADOLINIUM(1),

	// USPIO
	USPIO(2),

	// NONE
	NONE(3),

	// Multihance
	MULTIHANCE(5),

	// Prohance
	PROHANCE(6),

	// Gadovist
	GADOVIST(7);

	private int id;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            id
	 */
	private ContrastAgentUsed(final int id) {
		this.id = id;
	}

	/**
	 * Get a contrast agent by its id.
	 * 
	 * @param id
	 *            contrast agent id.
	 * @return contrast agent used.
	 */
	public static ContrastAgentUsed getConstrastAgent(final Integer id) {
		if (id == null) {
			return null;
		}
		for (ContrastAgentUsed constrastAgent : ContrastAgentUsed.values()) {
			if (id.equals(constrastAgent.getId())) {
				return constrastAgent;
			}
		}
		throw new IllegalArgumentException("No matching contrast agent used for id " + id);
	}
	
	/**
	 * Get an contrast agent Id by its name.
	 * 
	 * @param type
	 *            constrast Agent
	 * @return contrast Agent Id.
	 */
	public static ContrastAgentUsed getIdByType(final String type) {
		if (type == null) {
			return null;
		}
		return ContrastAgentUsed.valueOf(type);
	}
	

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

}
