package org.shanoir.challengeScores.migrator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.shanoir.challengeScores.migrator.model.Challenger;
import org.shanoir.challengeScores.migrator.model.Metric;
import org.shanoir.challengeScores.migrator.model.Patient;
import org.shanoir.challengeScores.migrator.model.Score;
import org.shanoir.challengeScores.migrator.model.Study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * Migrate challenge score data from Shanoir v1 to Shanoir ng
 */
public class MigratorMain {

	private static Logger LOGGER = new Logger();

	private static Properties properties;

	private static Set<Metric> metrics;
	private static Set<Challenger> challengers;
	private static Set<Study> studies;
	private static Set<Patient> patients;
	private static Map<Long, Score> scores;


	/**
	 * Main
	 *
	 * @param args
	 */
    public static void main(String[] args)
    {
    	try {
    		loadConfiguration();
			getDataFromShanoirV1();
			updateScores();

    	} catch (SQLException | IOException | ClassNotFoundException | RestCallException e) {
			LOGGER.error(e.getMessage());
		}

    }


    private static void loadConfiguration() throws IOException {
    	File file = new File("resources/config.properties");
		FileInputStream fileInput = new FileInputStream(file);
		properties = new Properties();
		properties.load(fileInput);
		fileInput.close();
	}


	private static void updateScores() throws RestCallException {
		ObjectNode json = getJsonBodyArg();
		//LOGGER.info(json.toString());
    	callRestService(json);
    }


	private static void callRestService(ObjectNode json) throws RestCallException {
		ClientConfig cc = new DefaultClientConfig();
    	cc.getClasses().add(JacksonJsonProvider.class);
    	Client client = Client.create(cc);
		WebResource webResource = client.resource("http://localhost:8080/score/all/reset");
		ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, json.toString());
		if (response.getStatus() != 204) {
			throw new RestCallException(new int[]{204}, response);
		}
	}


	private static ObjectNode getJsonBodyArg() {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode root = mapper.createObjectNode();

		ArrayNode studyList = mapper.createArrayNode();
		root.set("studyList", studyList);
		for (Study study : studies) {
			ObjectNode node = mapper.createObjectNode();
			node.put("id", study.getId());
			node.put("name", study.getName());
			studyList.add(node);
		}

		ArrayNode challengerList = mapper.createArrayNode();
		root.set("challengerList", challengerList);
		for (Challenger challenger : challengers) {
			ObjectNode node = mapper.createObjectNode();
			node.put("id", challenger.getId());
			node.put("name", challenger.getName());
			challengerList.add(node);
		}

		ArrayNode patientList = mapper.createArrayNode();
		root.set("patientList", patientList);
		for (Patient patient : patients) {
			ObjectNode node = mapper.createObjectNode();
			node.put("id", patient.getId());
			node.put("name", patient.getName());
			patientList.add(node);
		}

		ArrayNode metricList = mapper.createArrayNode();
		root.set("metricList", metricList);
		for (Metric metric : metrics) {
			ObjectNode node = mapper.createObjectNode();
			node.put("id", metric.getId());
			node.put("name", metric.getName());
			node.put("naN", metric.getNaN());
			node.put("negInf", metric.getNegInf());
			node.put("posInf", metric.getPosInf());
			ArrayNode studies = mapper.createArrayNode();
			if (metric.getStudies() != null) {
				for (Study study : metric.getStudies()) {
					ObjectNode studyNode = mapper.createObjectNode();
					studyNode.put("id", study.getId());
					studies.add(studyNode);
				}
			}
			node.set("studies", studies);
			metricList.add(node);
		}

		ArrayNode scoreList = mapper.createArrayNode();
		root.set("scoreList", scoreList);
		for (Score score : scores.values()) {
			ObjectNode node = mapper.createObjectNode();
			node.put("value", score.getValue());
			node.put("metricId", score.getMetric().getId());
			node.put("studyId", score.getStudy().getId());
			node.put("challengerId", score.getOwner().getId());
			node.put("patientId", score.getPatient().getId());
			node.put("inputDatasetId", score.getInputDatasetId());
			scoreList.add(node);
		}
		return root;
	}


    /**
     * Fetch old data from Shanoir V2 database
     *
     * @throws SQLException when a sql error occures
     * @throws IOException when cannot load query file
     * @throws ClassNotFoundException when cannot open a connection
     */
    private static void getDataFromShanoirV1() throws SQLException, IOException, ClassNotFoundException {
		Connection dbConnection = null;
		Statement statement = null;
		String queryStr = readFile(properties.getProperty("query.file"));
		dbConnection = getDBConnection();
		statement = dbConnection.createStatement();
		// execute select SQL stetement
		ResultSet rs = statement.executeQuery(queryStr);
		metrics = new HashSet<Metric>();
		challengers = new HashSet<Challenger>();
		studies = new HashSet<Study>();
		patients = new HashSet<Patient>();
		scores = new HashMap<Long, Score>();
		while (rs.next()) {

			Metric metric = new Metric();
			metric.setId(rs.getLong("METRIC_ID"));
			metric.setName(rs.getString("METRIC_NAME"));
			metric.setNaN(rs.getString("NAN"));
			metric.setNegInf(rs.getString("NINF"));
			metric.setPosInf(rs.getString("PINF"));
			metrics.add(metric);

			Challenger challenger = new Challenger(rs.getLong("USER_ID"));
			challenger.setName(rs.getString("USER_NAME"));
			challengers.add(challenger);

			Patient patient = new Patient( rs.getLong("SUBJECT_ID"));
			patient.setName(rs.getString("SUBJECT_NAME"));
			patients.add(patient);

			String studyName = rs.getString("STUDY_NAME");
			Study study = new Study(rs.getLong("STUDY_ID"));
			study.setName(studyName);
			studies.add(study);

			Long scoreId = rs.getLong("SCORE_ID");
			Float value = rs.getFloat("SCORE_VALUE");
			Long inputDatasetId = rs.getLong("INPUT_DATASET_ID");
			//Long outputDatasetId = rs.getLong("OUTPUT_DATASET_ID");
			//Long parent1DatasetId = rs.getLong("PARENT1_INPUT_DATASET_ID");
			//Long parent2DatasetId = rs.getLong("PARENT2_INPUT_DATASET_ID");
			Score score = new Score();
			score.setId(scoreId);
			score.setStudy(study);
			score.setMetric(metric);
			score.setOwner(challenger);
			score.setPatient(patient);
			score.setValue(value);
			if (studyName.contains("PET")) {
				score.setInputDatasetId(inputDatasetId);
			}
			scores.put(scoreId, score);
		}
	}


    /**
     * Opens a connection
     *
     * @return the Connection
     * @throws ClassNotFoundException if the driver clkass is not fuounded
     */
	private static Connection getDBConnection() throws ClassNotFoundException {
		Connection dbConnection = null;
		try {
			Class.forName(properties.getProperty("db.driver"));

		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		}
		try {
			dbConnection = DriverManager.getConnection(
					properties.getProperty("db.connection"),
					properties.getProperty("db.user"),
					properties.getProperty("db.password"));
			return dbConnection;
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		return dbConnection;
	}


	/**
	 * Read a file content
	 *
	 * @param fileName
	 * @return the content
	 * @throws IOException
	 */
	private static String readFile(String fileName) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(fileName));
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	        }
	        return sb.toString();
	    } finally {
	        br.close();
	    }
	}
}
