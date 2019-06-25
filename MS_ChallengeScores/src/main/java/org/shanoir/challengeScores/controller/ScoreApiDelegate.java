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

package org.shanoir.challengeScores.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.shanoir.challengeScores.data.access.service.ChallengerService;
import org.shanoir.challengeScores.data.access.service.MetricService;
import org.shanoir.challengeScores.data.access.service.PatientService;
import org.shanoir.challengeScores.data.access.service.ScoreService;
import org.shanoir.challengeScores.data.access.service.StudyService;
import org.shanoir.challengeScores.data.model.Challenger;
import org.shanoir.challengeScores.data.model.Metric;
import org.shanoir.challengeScores.data.model.Patient;
import org.shanoir.challengeScores.data.model.Score;
import org.shanoir.challengeScores.data.model.Study;
import org.shanoir.challengeScores.data.model.exception.RestServiceException;
import org.shanoir.challengeScores.data.model.exception.SeveralScoresException;
import org.shanoir.challengeScores.data.model.mapping.swagerapi.MetricMapper;
import org.shanoir.challengeScores.utils.Triplet;
import org.shanoir.challengeScores.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import io.swagger.api.MetricApiController;
import io.swagger.api.ScoreApiController;
import io.swagger.model.ChallengeScores;
import io.swagger.model.ChallengerSubjects;
import io.swagger.model.FlatScore;
import io.swagger.model.MetricChallengers;
import io.swagger.model.ResetObject;
import io.swagger.model.ScoreSet;
import io.swagger.model.SubjectScore;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Implement the logic for the generated Swagger server api : {@link MetricApiController}
 *
 * @author jlouis
 */
@Component
public class ScoreApiDelegate {

	@Autowired
	private ScoreService scoreService;

	@Autowired
	private MetricService metricService;

	@Autowired
	private StudyService studyService;

	@Autowired
	private ChallengerService challengerService;

	@Autowired
	private PatientService patientService;


	public static String XLS_FILE_NAME = "challenge";


	/**
	 * Constructor
	 */
	public ScoreApiDelegate() {
	}


	/**
	 * Save a score set.
	 * Implements the logic for the corresponding generated method :
	 * {@link ScoreApiController#saveScoreSet(Long, Long, Long, ScoreSet, Long)}
	 *
	 * @param studyId
	 * @param patientId
	 * @param ownerId
	 * @param scoreSet
	 * @param inputDatasetId
	 * @return {@link ResponseEntity}
	 */
	public ResponseEntity<Void> saveScoreSet(Long studyId, Long patientId, Long ownerId, ScoreSet scoreSet, Long inputDatasetId) {

		// Fetch all metrics once
		List<Metric> metrics = Utils.toList(metricService.findAll(studyId));

		List<Score> scores = new ArrayList<Score>();
		for (io.swagger.model.Score swaggerScore : scoreSet) {
			Score score = new Score();
			score.setMetric(findMetricByName(metrics, swaggerScore.getMetric()));
			score.setValue(swaggerScore.getValue());
			score.setInputDatasetId(inputDatasetId);
			score.setOwner(new Challenger(ownerId));
			score.setPatient(new Patient(patientId));
			score.setStudy(new Study(studyId));
			scores.add(score);
		}

		scoreService.saveAll(scores);
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	/**
	 * Find a metric by name among a given list
	 *
	 * @param metrics
	 * @param name
	 * @return the right metric
	 */
	private static Metric findMetricByName(List<Metric> metrics, String name) {
		for (Metric metric : metrics) {
			if (metric.getName().equals(name)) {
				return metric;
			}
		}
		return null;
	}


	/**
	 * Get all scores for this study.
	 * Manage the case when several scores exist for one patient (and one challenger and one metric).
	 *
	 * @param studyId
	 * @return a complex object ready to be serialized in json and looped over for builing a table, tr, td.
	 */
	public ResponseEntity<ChallengeScores> getScore(Long studyId) {
		List<Score> scores = Utils.toList(scoreService.getScores(studyId));
		ChallengeScores challengeScores = getTableFormattedScores(scores);
		return new ResponseEntity<ChallengeScores>(challengeScores, HttpStatus.OK);
	}


	/**
	 * Organize the scores so they are ready to be looped over for being displayed as a table.
	 *
	 * @param scores
	 * @return a Swagger defined object
	 */
	private ChallengeScores getTableFormattedScores(List<Score> scores) {
		// Build lists of all parameters
		Set<Metric> metrics = new HashSet<Metric>();
		Set<Challenger> challengers = new HashSet<Challenger>();
		Set<Patient> patients = new HashSet<Patient>();
		// Sorting?
		for (Score score : scores) {
			metrics.add(score.getMetric());
			challengers.add(score.getOwner());
			patients.add(score.getPatient());
		}

		// Build a map of the scores
		Map<Triplet<Metric, Challenger, Patient>, Set<Score>> scoreMap = buildScoreMap(scores);

		// Build the returned object
		ChallengeScores challengeScores = new ChallengeScores();
		for (Metric metric : metrics) {
			MetricChallengers metricChallenger = new MetricChallengers();
			metricChallenger.setMetricName(metric.getName());
			for (Challenger challenger : challengers) {
				ChallengerSubjects challengerSubjects = new ChallengerSubjects();
				challengerSubjects.setChallengerName(challenger.getName());
				for (Patient patient : patients) {
					Triplet<Metric, Challenger, Patient> coordinates = new Triplet<Metric, Challenger, Patient>(metric, challenger, patient);
					if (scoreMap.containsKey(coordinates)) {
						Set<Score> scoreSet = scoreMap.get(coordinates);
						if (scoreSet.size() == 1) { // When there is only one segmentation per patient
							SubjectScore subjectScore = new SubjectScore();
							subjectScore.setSubjectName(patient.getName());
							subjectScore.setScore(scoreSet.toArray(new Score[1])[0].getValue());
							challengerSubjects.addSubjectsItem(subjectScore);
						} else if (scoreSet.size() > 1) { // When there is several segmentation per patient -> split patient with the input dataset ids
							for (Score score : scoreSet) {
								SubjectScore subjectScore = new SubjectScore();
								StringBuilder subjectName = new StringBuilder();
								subjectName.append(patient.getName()).append(" (").append(score.getInputDatasetId()).append(")");
								subjectScore.setSubjectName(subjectName.toString());
								subjectScore.setScore(score.getValue());
								challengerSubjects.addSubjectsItem(subjectScore);
							}
						}
					}
				}
				metricChallenger.addChallengersItem(challengerSubjects);
			}
			challengeScores.add(metricChallenger);
		}
		return challengeScores;
	}


	/**
	 * Get all scores for this study as a XLS file
	 * Manage the case when several scores exist for one patient (and one challenger and one metric).
	 *
	 * @param studyId
	 * @return a complex object ready to be serialized in json and looped over for builing a table, tr, td.
	 * @throws RestServiceException
	 */
	public ResponseEntity<byte[]> getScoreXLS(Long studyId) throws RestServiceException {
		List<Score> scores = Utils.toList(scoreService.getScores(studyId));
		ChallengeScores challengeScores = getTableFormattedScores(scores);

		int startCol = 0;
		int startRow = 0;
		WorkbookSettings workBookSettings = new WorkbookSettings();
		workBookSettings.setLocale(Locale.ENGLISH);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		WritableWorkbook workbook;
		try {
			workbook = Workbook.createWorkbook(out, workBookSettings);
			int i = 0;
			for (MetricChallengers metricChallenger : challengeScores) {
				WritableSheet sheet = workbook.createSheet(metricChallenger.getMetricName(), i); // Create new tab
				int row = startRow;
				int headCol = startCol;
				sheet.setColumnView(headCol, 20); // Set 1st column width
				headCol++;
				/* Columns headers */
				if (!metricChallenger.getChallengers().isEmpty()) {
					for (SubjectScore patient : metricChallenger.getChallengers().get(0).getSubjects()) {
						sheet.setColumnView(headCol, 15); // Set column width
						sheet.addCell(new Label(headCol, row, patient.getSubjectName())); // Add header
						headCol++;
					}
					sheet.setColumnView(headCol, 15); // Set last column width
					sheet.addCell(new Label(headCol, row, "AVG")); // Add avg header
					row++;
					for (ChallengerSubjects challengerSubject : metricChallenger.getChallengers()) {
						int column = startCol;
						sheet.addCell(new Label(column++, row, challengerSubject.getChallengerName())); // Add row header
						for (SubjectScore subjectScore : challengerSubject.getSubjects()) {
							Float score = subjectScore.getScore();
							if (score != null) {
								sheet.addCell(new Number(column, row, score)); // Add score cell
							}
							column++;
						}
						/* Add an average column */
						StringBuilder strBuilder = new StringBuilder();
						strBuilder.append("AVERAGE(");
						strBuilder.append(toName(startCol+2));
						strBuilder.append(String.valueOf(row+1));
						strBuilder.append(":");
						strBuilder.append(toName(column));
						strBuilder.append(String.valueOf(row+1));
						strBuilder.append(")");
						Formula formula = new Formula(column, row , strBuilder.toString());
						sheet.addCell(formula);
						row++;
					}
					i++;
				}
			}
			workbook.write();
			workbook.close();
			byte[] content = out.toByteArray();
			HttpHeaders headers = new HttpHeaders();
		    headers.add("Content-Disposition", "attachment; filename=" + getXLSFileName(studyId));
			return new ResponseEntity<byte[]>(content, headers, HttpStatus.OK);

		} catch (IOException | WriteException e) {
			throw new RestServiceException(500, e.toString());
		}
	}

	private String getXLSFileName(Long studyId) {
		StringBuilder str = new StringBuilder();
		str.append(XLS_FILE_NAME);
		str.append("(").append(studyId).append(")_");
		str.append(new SimpleDateFormat("dd-MM-yyyy_HH-mm").format(new Date()));
		str.append(".xls");
		return str.toString();
	}


	/**
	 * Get column index from its name
	 * @param name
	 * @return {@link int}
	 */
	public static int toNumber(String name) {
        int number = 0;
        for (int i = 0; i < name.length(); i++) {
            number = number * 26 + (name.charAt(i) - ('A' - 1));
        }
        return number;
    }


	/**
	 * Get column name from its index
	 * @param name
	 * @return {@link String}
	 */
    public static String toName(int number) {
        StringBuilder sb = new StringBuilder();
        while (number-- > 0) {
            sb.append((char)('A' + (number % 26)));
            number /= 26;
        }
        return sb.reverse().toString();
    }


	/**
	 * Build a map of the scores
	 *
	 * @param scores
	 * @return a map
	 */
	private Map<Triplet<Metric, Challenger, Patient>, Set<Score>> buildScoreMap(List<Score> scores) {
		Map<Triplet<Metric, Challenger, Patient>, Set<Score>> scoreMap = new HashMap<Triplet<Metric, Challenger, Patient>, Set<Score>>();
		for (Score score : scores) {
			Set<Score> scoreSet;
			Triplet<Metric, Challenger, Patient> coordinates = new Triplet<Metric, Challenger, Patient>(score.getMetric(), score.getOwner(), score.getPatient());
			if (scoreMap.containsKey(coordinates)) {
				scoreSet = scoreMap.get(coordinates);
			} else {
				scoreSet = new HashSet<Score>();
			}
			scoreSet.add(score);
			scoreMap.put(coordinates, scoreSet);
		}
		return scoreMap;
	}


	public ResponseEntity<ScoreSet> getScoreSet(Long studyId, Long patientId, Long ownerId, Long inputDatasetId) throws SeveralScoresException {
		// Get all the scores for the study
		ScoreSet scoreSet = new ScoreSet();
		for (Score score : getUniqueScoreSet(studyId, patientId, ownerId, inputDatasetId)) {
			io.swagger.model.Score swaggerScore = new io.swagger.model.Score();
			swaggerScore.setMetric(score.getMetric().getName());
			swaggerScore.setValue(score.getValue());
			scoreSet.add(swaggerScore);
		}
		if (scoreSet.isEmpty()) {
			return new ResponseEntity<ScoreSet>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<ScoreSet>(scoreSet, HttpStatus.OK);
	}


	private Set<Score> getUniqueScoreSet(Long studyId, Long patientId, Long ownerId, Long inputDatasetId) throws SeveralScoresException {
		List<Score> scores = Utils.toList(scoreService.getScores(studyId));
		Set<Score> scoreSet = new HashSet<Score>();

		// Get metric list without a new query
		Set<Metric> metrics = new HashSet<Metric>();
		for (Score score : scores) {
			metrics.add(score.getMetric());
		}

		// Build a map of the scores
		Map<Triplet<Metric, Challenger, Patient>, Set<Score>> scoreMap = buildScoreMap(scores);

		// Build the ScoreSet
		for (Metric metric : metrics) {
			Triplet<Metric, Challenger, Patient> key = new Triplet<Metric, Challenger, Patient>(metric, new Challenger(ownerId), new Patient(patientId));
			Set<Score> scoresFounded = scoreMap.get(key);
			if (scoresFounded != null && scoresFounded.size() == 1) { // If only one score founded
				scoreSet.add(scoresFounded.toArray(new Score[1])[0]);
			} else if (scoresFounded != null && scoresFounded.size() > 1) { // If several scores founded
				if (inputDatasetId != null) {
					// Try to match the input dataset id
					for (Score score : scoresFounded) {
						if (score.getInputDatasetId().equals(inputDatasetId)) {
							scoreSet.add(score);
							break;
						}
					}
				} else { // Else it is an error
					List<Long> ids = new ArrayList<Long>();
					for (Score score : scoresFounded) {
						ids.add(score.getInputDatasetId());
					}
					throw new SeveralScoresException(409, ids, studyId, null, ownerId, patientId);
				}
			}
		}
		return scoreSet;
	}


	public ResponseEntity<Void> deleteScoreSet(Long studyId, Long patientId, Long ownerId, Long inputDatasetId) throws SeveralScoresException {
		Set<Score> scoreSet = getUniqueScoreSet(studyId, patientId, ownerId, inputDatasetId);
		if (scoreSet.isEmpty()) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		} else {
			scoreService.deleteAll(scoreSet);
		}
		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}


	public ResponseEntity<Void> resetScores(ResetObject scoreList) {

		List<Challenger> challengers = new ArrayList<Challenger>();
		for (io.swagger.model.Challenger swaggerChallenger : scoreList.getChallengerList()) {
			Challenger challenger = new Challenger();
			challenger.setId(swaggerChallenger.getId().longValue());
			challenger.setName(swaggerChallenger.getName());
			challengers.add(challenger);
		}

		List<Patient> patients = new ArrayList<Patient>();
		for (io.swagger.model.Patient swaggerPatient: scoreList.getPatientList()) {
			Patient patient = new Patient(swaggerPatient.getId().longValue());
			patient.setName(swaggerPatient.getName());
			patients.add(patient);
		}

		List<Study> studies = new ArrayList<Study>();
		for (io.swagger.model.Study swaggerStudy : scoreList.getStudyList()) {
			Study study = new Study(swaggerStudy.getId().longValue());
			study.setName(swaggerStudy.getName());
			studies.add(study);
		}

		List<Metric> metrics = MetricMapper.swaggerToModel(scoreList.getMetricList());

		List<Score> scores = new ArrayList<Score>();
		for (FlatScore flatScore : scoreList.getScoreList()) {
			Score score = new Score();
			Metric metric = new Metric();
			metric.setId(flatScore.getMetricId().longValue());
			score.setValue(flatScore.getValue());
			score.setMetric(metric);
			score.setOwner(new Challenger(flatScore.getChallengerId().longValue()));
			score.setPatient(new Patient(flatScore.getPatientId().longValue()));
			score.setStudy(new Study(flatScore.getStudyId().longValue()));
			score.setInputDatasetId(flatScore.getInputDatasetId() != null ? flatScore.getInputDatasetId().longValue() : null);
			scores.add(score);
		}

		scoreService.deleteAll();
		metricService.deleteAll();
		studyService.deleteAll();
		patientService.deleteAll();
		challengerService.deleteAll();

		challengerService.saveAll(challengers);
		patientService.saveAll(patients);
		studyService.saveAll(studies);
		metricService.saveAll(metrics);
		scoreService.saveAll(scores);

		return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
	}
}
