package io.swagger.api;

import org.shanoir.challengeScores.controller.ChallengerApiDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;
import io.swagger.model.Challengers;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.SpringCodegen", date = "2016-10-11T09:18:12.164Z")

@Controller
public class ChallengerApiController implements ChallengerApi {

	@Autowired
	private ChallengerApiDelegate challengerApiDelegate;

	public ResponseEntity<Void> deleteAllChallengers() {
        return challengerApiDelegate.deleteAll();
    }

	public ResponseEntity<Void> saveChallenger(
			@ApiParam(value = "id of the challenger", required = true) @RequestParam(value = "id", required = true) Long id,
			@ApiParam(value = "name of the challenger", required = true) @RequestParam(value = "name", required = true) String name) {
		return challengerApiDelegate.saveChallenger(id, name);
	}

	public ResponseEntity<Void> updateChallengers(@ApiParam(value = "the challengers to save", required = true) @RequestBody Challengers challengers) {
		return challengerApiDelegate.updateAll(challengers);
	}

}
