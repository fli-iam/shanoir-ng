package org.shanoir.ng.vip.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.HttpStatusCodeException;

@Controller
public class PipelineApiController implements PipelineApi {

    @Autowired
    private VipClientService vipClient;

    /**
     *
     * @param identifier
     * @return
     */
    @Override
    public ResponseEntity<String> getPipeline(String identifier) {
        String json;
        try {
            json = vipClient.getPipeline(identifier);
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(json, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> getPipelineAll() {

        String json;
        try {
            json = vipClient.getPipelineAll();
        } catch (HttpStatusCodeException e) {
            // in case of an error with response payload
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(json, HttpStatus.OK);
    }
}
