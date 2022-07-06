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

package org.shanoir.ng.importer;

import io.swagger.annotations.*;

import org.shanoir.ng.importer.model.carmin.Path;
import org.shanoir.ng.importer.model.carmin.UploadData;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Api(value = "carmin-data")
@RequestMapping("/carmin-data")
public interface CarminDataApi {

    @ApiOperation(value = "Delete a path", notes = "Delete a path and transitively delete all its content if it is a directory.", tags={  })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "the deletion is successful and finished."),

            @ApiResponse(code = 200, message = "A functional or internal error occured processing the request") })
    @RequestMapping(value = "/path/{completePath}",
            produces = { "application/json" },
            method = RequestMethod.DELETE)
    ResponseEntity<Void> deletePath(@ApiParam(value = "The complete path to delete. It can contain non-encoded slashes.", required=true) @PathVariable("completePath") String completePath);


    @ApiOperation(value = "Upload data to a path", tags = "A request without content creates a directory (an error should be returned if the path already exists). A request with a specific content type (\"application/carmin+json\") allows to upload data encoded in base64. The base64 content (part of a json payload) can either be an encoded file, are an encoded zip archive that will create a directory. All other content (with any content type) will be considered as a raw file and will override the existing path content. If the parent directory of the file/directory to create does not exist, an error must be returned.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "The upload is successful and finished.", response = Path.class),

            @ApiResponse(code = 200, message = "A functional or internal error occured processing the request") })
    @RequestMapping(value = "/**",
            produces = { "application/json" },
            consumes = { "application/carmin+json", "application/octet-stream" },
            method = RequestMethod.PUT)
    ResponseEntity<Path> uploadPath(@ApiParam(value = "") @Valid @RequestBody UploadData body) throws RestServiceException;

}