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

package org.shanoir.challengeScores.data.model.exception;

import java.util.ArrayList;
import java.util.List;

import org.shanoir.challengeScores.utils.Utils;

public class SeveralScoresException extends RestServiceException {

	private static final long serialVersionUID = 1L;

	private List<Long> inputDatasetIds;
	private Long studyId;
	private Long metricId;
	private Long challengerId;
	private Long patientId;


	/**
	 * @param inputDatasetIds
	 * @param studyId
	 * @param metricId
	 * @param challengerId
	 * @param patientId
	 */
	public SeveralScoresException(int code, List<Long> inputDatasetIds, Long studyId, Long metricId, Long challengerId, Long patientId) {
		super(code, null);
		this.inputDatasetIds = inputDatasetIds;
		this.studyId = studyId;
		this.metricId = metricId;
		this.challengerId = challengerId;
		this.patientId = patientId;
		this.setMessage(buildMessage());
	}


	private String buildMessage() {
		StringBuilder msg = new StringBuilder();
		msg.append("Found ").append(inputDatasetIds.size());
		msg.append(" possible values. You should add an input dataset id parameter in your request. Founded values have those input dataset ids : [");
		msg.append(Utils.join(inputDatasetIds, ", ")).append("].");
		msg.append(" You used those parameters as inputs : ");
		List<String> params = new ArrayList<String>();
		if (studyId != null) params.add(new StringBuilder().append("studyId = ").append(studyId).toString());
		if (metricId != null) params.add(new StringBuilder().append("metricId = ").append(metricId).toString());
		if (challengerId != null) params.add(new StringBuilder().append("ownerId = ").append(challengerId).toString());
		if (patientId != null) params.add(new StringBuilder().append("patientId = ").append(patientId).toString());
		msg.append(Utils.join(params, ", ")).append(".");
		return super.toString() + msg;
	}

}
