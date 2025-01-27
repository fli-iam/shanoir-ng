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

package org.shanoir.ng.vip.pipeline.controler;

import org.shanoir.ng.vip.pipeline.service.VipClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PipelineApiController implements PipelineApi {

    @Autowired
    private VipClientService vipClient;

    @Override
    public ResponseEntity<String> getPipelineAll() {
        return ResponseEntity.ok(vipClient.getPipelineAll().block());
    }

    /**
     * @param identifier
     * @return
     */
    @Override
    public ResponseEntity<String> getPipeline(String identifier, String version) {
        return ResponseEntity.ok(vipClient.getPipeline(identifier, version).block());
    }
}
