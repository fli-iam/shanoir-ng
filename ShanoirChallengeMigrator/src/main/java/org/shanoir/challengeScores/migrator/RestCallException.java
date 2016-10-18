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
