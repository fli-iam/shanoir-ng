package org.shanoir.ng.migration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.shanoir.ng.shared.core.model.IdName;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirException;
import org.shanoir.ng.shared.migration.DistantKeycloakConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class MigrationApiController implements MigrationApi {

	@Autowired
	StudyMigrationService migrationService;

	@Autowired
	DistantKeycloakConfigurationService keycloakService;

	private static final Logger LOG = LoggerFactory.getLogger(MigrationApiController.class);

	@Value("${migration-servers}")
	private String urlsAsString;

	Map<Integer, String> urlsAsMap;
	
	List<IdName> urls;

	@Override
	public synchronized ResponseEntity<String> migrateStudy (
			@ApiParam(value = "Url of distant shanoir") @RequestParam("shanoirUrl") Integer shanoirUrl,
			@ApiParam(value = "Username of user") @RequestParam("username") String username,
			@ApiParam(value = "Password of user") @RequestParam("userPassword") String userPassword,
			@ApiParam(value = "study ID", required = true) @RequestParam("studyId") Long studyId,
			@ApiParam(value = "Distant user ID", required = true) @RequestParam("userId") Long userId)
					throws RestServiceException {
		try {
			// Connect to keycloak and keep connection alive
			keycloakService.connectToDistantKeycloak(urlsAsMap.get(shanoirUrl), username, userPassword);

			// Migrate study
			this.migrationService.migrateStudy(studyId, userId, username, urlsAsMap.get(shanoirUrl));
		} catch (ShanoirException e) {
			LOG.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			LOG.error("ERROR: Unexpected error while migrating study {} :", studyId, e);
			return new ResponseEntity<>("ERROR: Unexpected error while migrating study, pease contact an administrator", HttpStatus.INTERNAL_SERVER_ERROR);
		} finally {
			keycloakService.stop();
		}
		return new ResponseEntity<>("Success", HttpStatus.OK);
	}

	@Override
	public ResponseEntity<List<IdName>> getMigrationConfig() throws IOException {
		if (urls == null) {
			urlsAsMap = new HashMap<>();
			urls = new ArrayList<>();
			int i = 0;
			for (String element : urlsAsString.split(";;")) {
				urls.add(new IdName(Long.valueOf(i), element.split("==")[0]));
				urlsAsMap.put(i++, element.split("==")[1]);
			}
		}
		return new ResponseEntity<>(this.urls, HttpStatus.OK);
	}

}
