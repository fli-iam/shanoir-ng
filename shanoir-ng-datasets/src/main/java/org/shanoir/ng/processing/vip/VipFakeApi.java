package org.shanoir.ng.processing.vip;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please do not use this API in production
 * This API is here to fake a VIP call to list processing and result of a processing.
 * @author jcome
 *
 */
@RequestMapping("/fakevip")
public interface VipFakeApi {
	
    @GetMapping(value = "", produces = { "application/json" }, consumes = {
                    "application/json" })
	public ResponseEntity<String> getProcessing();

    @PostMapping(value = "", produces = { "application/json" }, consumes = {
    "application/json" })
	public void launchProcessing();

}
