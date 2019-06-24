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

package org.shanoir.challengeScores.migrator;import java.util.Arrays;

import com.sun.jersey.api.client.ClientResponse;

public class RestCallException extends Exception {

	private static final long serialVersionUID = 1L;

	private int[] possibleCodes;
	private ClientResponse response;

	/**
	 * @param possibleCodes
	 * @param obtained
	 */
	public RestCallException(int[] possibleCodes, ClientResponse response) {
		this.possibleCodes = possibleCodes;
		this.response = response;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Call to REST service failed : return code was ");
		sb.append(response.getStatus());
		sb.append(" instead of ");
		sb.append(Utils.join(Arrays.asList(possibleCodes), ", "));
		sb.append(".");
		if (response.getStatus() == 500) {
			sb.append(" Returned message : ").append(response.getEntity(String.class));
		}
		return super.toString();
	}


}
