package org.shanoir.ng.dataset.controler;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiParam;
import org.shanoir.ng.dataset.model.carmin.GetPathResponse;
import org.shanoir.ng.dataset.model.carmin.Path;
import org.shanoir.ng.dataset.model.carmin.UploadData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController
public class CarminDAtaApiController implements CarminDataApi{

    private static final Logger log = LoggerFactory.getLogger(CarminDAtaApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    public CarminDAtaApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    public ResponseEntity<Void> deletePath(@ApiParam(value = "The complete path to delete. It can contain non-encoded slashes.", required=true) @PathVariable("completePath") String completePath) {
        String accept = request.getHeader("Accept");
        return new ResponseEntity<Void>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<GetPathResponse> getPath(@ApiParam(value = "the complete path on which to request information. It can contain non-encoded slashes. Except for the \"exists\" action, any request on a non-existing path should return an error", required=true) @PathVariable("completePath") String completePath,@NotNull @ApiParam(value = "The \"content\" action downloads the raw file. If the path points to a directory, a tarball of this directory is returned. The \"exists\" action returns a BooleanResponse object (see definition) indicating if the path exists or not. The \"properties\" action returns a Path object (see definition) with the path properties. The \"list\" action returns a DirectoryList object (see definition) with the properties of all the files of the directory (if the path is not a directory an error must be returned). The \"md5\" action is optional and returns a PathMd5 object (see definition)." ,required=true
    ) @Valid @RequestParam(value = "action", required = true) String action) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<GetPathResponse>(objectMapper.readValue("\"\"", GetPathResponse.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<GetPathResponse>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<GetPathResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

    public ResponseEntity<Path> uploadPath(@ApiParam(value = "The complete path on which to upload data. It can contain non-encoded slashes.", required=true) @PathVariable("completePath") String completePath,@ApiParam(value = "") @Valid @RequestBody UploadData body) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            try {
                return new ResponseEntity<Path>(objectMapper.readValue("{\n  \"executionId\" : \"executionId\",\n  \"lastModificationDate\" : 0,\n  \"size\" : 6,\n  \"platformPath\" : \"platformPath\",\n  \"mimeType\" : \"mimeType\",\n  \"isDirectory\" : true\n}", Path.class), HttpStatus.NOT_IMPLEMENTED);
            } catch (IOException e) {
                log.error("Couldn't serialize response for content type application/json", e);
                return new ResponseEntity<Path>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        return new ResponseEntity<Path>(HttpStatus.NOT_IMPLEMENTED);
    }
}
