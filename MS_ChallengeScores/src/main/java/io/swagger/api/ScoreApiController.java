package io.swagger.api;

import org.shanoir.challengeScores.controller.ScoreApiDelegate;
import org.shanoir.challengeScores.data.model.exception.RestServiceException;
import org.shanoir.challengeScores.data.model.exception.SeveralScoresException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;
import io.swagger.model.ChallengeScores;
import io.swagger.model.ResetObject;
import io.swagger.model.ScoreSet;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

@Controller
public class ScoreApiController implements ScoreApi {

	@Autowired
	private ScoreApiDelegate scoreApiDelegate;

	public ResponseEntity<Void> deleteScoreSet(
			@ApiParam(value = "id of the study", required = true) @RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "patient (subject) of the score set", required = true) @RequestParam(value = "patientId", required = true) Long patientId,
			@ApiParam(value = "owner of the score set", required = true) @RequestParam(value = "ownerId", required = true) Long ownerId,
			@ApiParam(value = "input dataset id (should be the same for many segmentations)") @RequestParam(value = "inputDatasetId", required = false) Long inputDatasetId) throws SeveralScoresException {

		return scoreApiDelegate.deleteScoreSet(studyId, patientId, ownerId, inputDatasetId);
	}

	public ResponseEntity<ScoreSet> deleteScoreSetByDatasetId(@ApiParam(value = "id of the segmentation result dataset", required = true) @PathVariable("segmentedDatasetId") Long segmentedDatasetId) {
		// do some magic!
		return new ResponseEntity<ScoreSet>(HttpStatus.OK); // TODO
	}

	public ResponseEntity<ScoreSet> findScoreSet(
			@ApiParam(value = "id of the study", required = true) @RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "patient (subject) of the score set", required = true) @RequestParam(value = "patientId", required = true) Long patientId,
			@ApiParam(value = "owner of the score set", required = true) @RequestParam(value = "ownerId", required = true) Long ownerId,
			@ApiParam(value = "input dataset id (should be the same for many segmentations)") @RequestParam(value = "inputDatasetId", required = false) Long inputDatasetId) throws SeveralScoresException {

		return scoreApiDelegate.getScoreSet(studyId, patientId, ownerId, inputDatasetId);
	}

	public ResponseEntity<ScoreSet> findScoreSetByDatasetId(@ApiParam(value = "id of the segmentation result dataset", required = true) @PathVariable("segmentedDatasetId") Long segmentedDatasetId) {

		return new ResponseEntity<ScoreSet>(HttpStatus.OK);
	}

	public ResponseEntity<ChallengeScores> findScoresByStudyId(@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) {
		return scoreApiDelegate.getScore(studyId);
	}

	public ResponseEntity<byte[]> getXLSScoresByStudyId(@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId) throws RestServiceException {
		return scoreApiDelegate.getScoreXLS(studyId);
	}

	public ResponseEntity<Void> resetScores(@ApiParam(value = "the new data", required = true) @RequestBody ResetObject resetObject) {
		return scoreApiDelegate.resetScores(resetObject);
	}

	public ResponseEntity<Void> saveScoreSet(
			@ApiParam(value = "id of the study", required = true) @RequestParam(value = "studyId", required = true) Long studyId,
			@ApiParam(value = "patient (subject) of the score set", required = true) @RequestParam(value = "patientId", required = true) Long patientId,
			@ApiParam(value = "owner of the score set", required = true) @RequestParam(value = "ownerId", required = true) Long ownerId,
			@ApiParam(value = "the score set to save", required = true) @RequestBody ScoreSet scoreSet,
			@ApiParam(value = "input dataset id (should be the same for many segmentations)") @RequestParam(value = "inputDatasetId", required = false) Long inputDatasetId) {

		return scoreApiDelegate.saveScoreSet(studyId, patientId, ownerId, scoreSet, inputDatasetId);
	}

	public ResponseEntity<Void> saveScoreSetByDatasetId(
			@ApiParam(value = "id of the segmentation result dataset", required = true) @PathVariable("segmentedDatasetId") Long segmentedDatasetId,
			@ApiParam(value = "the score set to save", required = true) @RequestBody ScoreSet scoreSet) {
		return new ResponseEntity<Void>(HttpStatus.OK); // TODO
	}

}
