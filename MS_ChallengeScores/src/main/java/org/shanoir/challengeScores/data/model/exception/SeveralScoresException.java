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
