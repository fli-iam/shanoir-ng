package org.shanoir.ng.key.controller;

import org.shanoir.ng.key.service.KeyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class KeyValueApiController implements KeyValueApi {
	
    @Autowired
	private KeyValueService keyValueService;

	@Override
	public ResponseEntity<String> findValue(@PathVariable String key) {
        String value = keyValueService.getValue(key);
        if (value == null) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(value);
        }
    }

}
