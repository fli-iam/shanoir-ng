package io.swagger.api;

import org.shanoir.challengeScores.data.model.exception.RestServiceException;
import org.shanoir.challengeScores.data.model.exception.SeveralScoresException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.model.ChallengeScores;
import io.swagger.model.ResetObject;
import io.swagger.model.ScoreSet;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

@Api(value = "score", description = "the score API")
public interface ScoreApi {

	@ApiOperation(value = "", notes = "Deletes a score set", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "score set deleted", response = Void.class),
			@ApiResponse(code = 404, message = "no score set founded", response = Void.class),
			@ApiResponse(code = 409, message = "several score sets founded, need more data, see error", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/score", produces = { "application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<Void> deleteScoreSet(
			@ApiParam(value = "id of the study", required = true) @RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "patient (subject) of the score set", required = true) @RequestParam(value = "patientId", required = true) Long patientId,
			@ApiParam(value = "owner of the score set", required = true) @RequestParam(value = "ownerId", required = true) Long ownerId,
			@ApiParam(value = "input dataset id (should be the same for many segmentations)") @RequestParam(value = "inputDatasetId", required = false) Long inputDatasetId)
			throws SeveralScoresException;

	@ApiOperation(value = "", notes = "If exists, deletes the score set", response = ScoreSet.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "score set", response = ScoreSet.class),
			@ApiResponse(code = 404, message = "no score set founded", response = ScoreSet.class),
			@ApiResponse(code = 200, message = "unexpected error", response = ScoreSet.class) })
	@RequestMapping(value = "/score/{segmentedDatasetId}", produces = {
			"application/json" }, method = RequestMethod.DELETE)
	ResponseEntity<ScoreSet> deleteScoreSetByDatasetId(
			@ApiParam(value = "id of the segmentation result dataset", required = true) @PathVariable("segmentedDatasetId") Long segmentedDatasetId);

	@ApiOperation(value = "", notes = "If exists, returns the score set for the given subject + owner", response = ScoreSet.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "score set", response = ScoreSet.class),
			@ApiResponse(code = 404, message = "no score set founded", response = ScoreSet.class),
			@ApiResponse(code = 409, message = "several score sets founded, see error msg", response = ScoreSet.class),
			@ApiResponse(code = 200, message = "unexpected error", response = ScoreSet.class) })
	@RequestMapping(value = "/score", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<ScoreSet> findScoreSet(
			@ApiParam(value = "id of the study", required = true) @RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "patient (subject) of the score set", required = true) @RequestParam(value = "patientId", required = true) Long patientId,
			@ApiParam(value = "owner of the score set", required = true) @RequestParam(value = "ownerId", required = true) Long ownerId,
			@ApiParam(value = "input dataset id (should be the same for many segmentations)") @RequestParam(value = "inputDatasetId", required = false) Long inputDatasetId)
			throws SeveralScoresException;

	@ApiOperation(value = "", notes = "If exists, returns the score set", response = ScoreSet.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "score set", response = ScoreSet.class),
			@ApiResponse(code = 404, message = "no score set founded", response = ScoreSet.class),
			@ApiResponse(code = 200, message = "unexpected error", response = ScoreSet.class) })
	@RequestMapping(value = "/score/{segmentedDatasetId}", produces = {
			"application/json" }, method = RequestMethod.GET)
	ResponseEntity<ScoreSet> findScoreSetByDatasetId(
			@ApiParam(value = "id of the segmentation result dataset", required = true) @PathVariable("segmentedDatasetId") Long segmentedDatasetId);

	@ApiOperation(value = "", notes = "Returns the scores attached to the given study", response = ChallengeScores.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "score matrix", response = ChallengeScores.class),
			@ApiResponse(code = 200, message = "unexpected error", response = ChallengeScores.class) })
	@RequestMapping(value = "/score/all/{studyId}", produces = { "application/json" }, method = RequestMethod.GET)
	ResponseEntity<ChallengeScores> findScoresByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId);

	@ApiOperation(value = "", notes = "Returns the scores attached to the given study, as a Excel file", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Challenge scores Excel file", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/score/all/xls/{studyId}", produces = {
			"application/vnd.ms-excel" }, method = RequestMethod.GET)
	ResponseEntity<byte[]> getXLSScoresByStudyId(
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId)
			throws RestServiceException;

	@ApiOperation(value = "", notes = "Reset all scores", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "Database reset", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/score/all/reset", produces = { "application/json" }, method = RequestMethod.POST)
	ResponseEntity<Void> resetScores(
			@ApiParam(value = "the new data", required = true) @RequestBody ResetObject resetObject
	);

	@ApiOperation(value = "", notes = "Creates/overwrite a score set", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 204, message = "scores saved", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/score", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Void> saveScoreSet(
			@ApiParam(value = "id of the study", required = true) @RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "patient (subject) of the score set", required = true) @RequestParam(value = "patientId", required = true) Long patientId,
			@ApiParam(value = "owner of the score set", required = true) @RequestParam(value = "ownerId", required = true) Long ownerId,
			@ApiParam(value = "the score set to save", required = true) @RequestBody ScoreSet scoreSet,
			@ApiParam(value = "input dataset id (should be the same for many segmentations)") @RequestParam(value = "inputDatasetId", required = false) Long inputDatasetId);

	@ApiOperation(value = "", notes = "Creates/overwrite a score set", response = Void.class, tags = {})
	@ApiResponses(value = { @ApiResponse(code = 201, message = "score created", response = Void.class),
			@ApiResponse(code = 204, message = "score overwritten", response = Void.class),
			@ApiResponse(code = 200, message = "unexpected error", response = Void.class) })
	@RequestMapping(value = "/score/{segmentedDatasetId}", produces = { "application/json" }, consumes = {
			"application/json" }, method = RequestMethod.POST)
	ResponseEntity<Void> saveScoreSetByDatasetId(
			@ApiParam(value = "id of the segmentation result dataset", required = true) @PathVariable("segmentedDatasetId") Long segmentedDatasetId,
			@ApiParam(value = "the score set to save", required = true) @RequestBody ScoreSet scoreSet);

}
