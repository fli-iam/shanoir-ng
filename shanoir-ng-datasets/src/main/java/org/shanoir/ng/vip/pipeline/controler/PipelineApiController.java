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

import org.shanoir.ng.vip.pipeline.service.PipelineServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PipelineApiController implements PipelineApi {

    @Autowired
    private PipelineServiceImpl pipelineService;

    public ResponseEntity<String> getPipelineAll() {
        return ResponseEntity.ok(pipelineService.getPipelineAll().block());
    }

    public ResponseEntity<String> getPipeline(String identifier, String version) {
        return ResponseEntity.ok(pipelineService.getPipeline(identifier, version).block());
    }
}
