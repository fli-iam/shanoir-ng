package org.shanoir.ng.migration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

import org.apache.http.ParseException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import io.swagger.annotations.ApiParam;

@Controller
public class MigrationApiController implements MigrationApi {

	@Autowired
	RestTemplate restTemplate;

	@Override
	public ResponseEntity<String> migrateStudy (
					@ApiParam(value = "Url of distant shanoir") @RequestParam("shanoirUrl") String shanoirUrl,
					@ApiParam(value = "Username of user") @RequestParam("username") String username,
					@ApiParam(value = "Password of user") @RequestParam("userPassword") String userPassword,
					@ApiParam(value = "study ID", required = true) @RequestParam("studyId") Long studyId)
		throws RestServiceException {

		// Connect to keycloak and keep connection alive
		String keycloakURL = shanoirUrl + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
		try {
			final StringBuilder postBody = new StringBuilder();
			postBody.append("client_id=shanoir-uploader");
			postBody.append("&grant_type=password");
			postBody.append("&username=").append(URLEncoder.encode(username, "UTF-8"));
			postBody.append("&password=").append(URLEncoder.encode(userPassword, "UTF-8"));
			postBody.append("&scope=offline_access");
			
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
			headers.set("Content-type", "application/x-www-form-urlencoded");

			ResponseEntity<String> response = restTemplate.exchange(keycloakURL, HttpMethod.POST, new HttpEntity<>(postBody.toString(), headers), String.class);

			// Migrate study

			return new ResponseEntity<>(response.getBody(), HttpStatus.OK);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
