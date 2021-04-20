package org.shanoir.ng.migration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.http.ParseException;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import io.swagger.annotations.ApiParam;

@Controller
public class MigrationApiController implements MigrationApi {

	@Autowired
	RestTemplate restTemplate;

	@Override
	public ResponseEntity<Void> migrateStudy (
			@ApiParam(value = "id of the study", required = true) @PathVariable("studyId") Long studyId,
			@ApiParam(value = "url of distant shanoir", required = true) @RequestBody String shanoirUrl)
					throws RestServiceException {
		throw new NotImplementedException("Not implemented yet !");
	}

	@Override
	public ResponseEntity<Void> connect (
					@ApiParam(value = "Url of distant shanoir") @RequestParam("shanoirUrl") String shanoirUrl,
					@ApiParam(value = "Username of user") @RequestParam("username") String username,
					@ApiParam(value = "Password of user") @RequestParam("userPassword") String userPassword)
		throws RestServiceException {

		String keycloakURL = shanoirUrl + "/auth/realms/shanoir-ng/protocol/openid-connect/token";
		try {
			final StringBuilder postBody = new StringBuilder();
			postBody.append("client_id=shanoir-uploader");
			postBody.append("&grant_type=password");
			postBody.append("&username=").append(URLEncoder.encode(username, "UTF-8"));
			postBody.append("&password=").append(URLEncoder.encode(userPassword, "UTF-8"));
			postBody.append("&scope=offline_access");

			ResponseEntity<String> response = restTemplate.exchange(keycloakURL, HttpMethod.POST, new HttpEntity<>(postBody), String.class);
			String responseEntityString = response.getBody();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
