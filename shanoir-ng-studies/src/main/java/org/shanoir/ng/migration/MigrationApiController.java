package org.shanoir.ng.migration;

import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.annotations.ApiParam;

@Controller
public class MigrationApiController implements MigrationApi {

	@Autowired
	MigrationService migrationService;

	@Autowired
	DistantKeycloakConfigurationService keycloakService;

	@Override
	public synchronized ResponseEntity<String> migrateStudy (
			@ApiParam(value = "Url of distant shanoir") @RequestParam("shanoirUrl") String shanoirUrl,
			@ApiParam(value = "Username of user") @RequestParam("username") String username,
			@ApiParam(value = "Password of user") @RequestParam("userPassword") String userPassword,
			@ApiParam(value = "study ID", required = true) @RequestParam("studyId") Long studyId,
			@ApiParam(value = "Distant user ID", required = true) @RequestParam("userId") Long userId)
					throws RestServiceException {

		// Connect to keycloak and keep connection alive
		keycloakService.connectToDistantKeycloak(shanoirUrl, username, userPassword);

		// Migrate study
		this.migrationService.migrateStudy(studyId, userId);

		return null;
	}

}
