package org.shanoir.ng.dicom.web;

import java.util.Map;

import javax.validation.Valid;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This implements the DICOMWeb REST-API exposed by Shanoir-NG.
 * For more informations please see:
 * 
 * https://github.com/fli-iam/shanoir-ng/wiki/DICOMWeb
 * 
 * @author mkain
 *
 */
@Api(value = "dicomweb")
@RequestMapping("/dicomweb")
public interface DICOMWebApi {

	@ApiOperation(value = "", notes = "Returns all DICOM patients/subjects", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found patients/subjects", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no patient/subject found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/patients", produces = { "application/dicom+json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<String> findPatients() throws RestServiceException;

	@ApiOperation(value = "", notes = "Returns all DICOM studies/examinations", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found studies/examinations", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no study/exam found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/studies", produces = { "application/dicom+json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<String> findStudies(@RequestParam Map<String,String> allParams) throws RestServiceException, JsonMappingException, JsonProcessingException;

	@ApiOperation(value = "", notes = "Returns all DICOM series/acquisitions", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found series/acquisitions", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no serie/acquisition found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/series", produces = { "application/dicom+json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<String> findSeries() throws RestServiceException;

	@ApiOperation(value = "", notes = "Returns all DICOM series/acquisitions of an examination", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found series/acquisitions", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no serie/acquisition found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/studies/{examinationId}/series", produces = { "application/dicom+json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	ResponseEntity<String> findSeriesOfStudy(
			@ApiParam(value = "examinationId", required = true) @PathVariable("examinationId") Long examinationId
		) throws RestServiceException, JsonMappingException, JsonProcessingException;
	
	@ApiOperation(value = "", notes = "Returns the metadata of a DICOM serie/acquisition of an examination", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found serie/acquisition metadata", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no serie/acquisition metadata found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/studies/{examinationId}/series/{serieInstanceUID}/metadata", produces = { "application/dicom+json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	ResponseEntity<String> findSerieMetadataOfStudy(
			@ApiParam(value = "examinationId", required = true) @PathVariable("examinationId") Long examinationId,
			@ApiParam(value = "serieInstanceUID", required = true) @PathVariable("serieInstanceUID") String serieInstanceUID
		) throws RestServiceException, JsonMappingException, JsonProcessingException;

	@ApiOperation(value = "", notes = "Returns all DICOM instances/datasets of a study and serie", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found instances/datasets", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no instance/dataset found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/studies/{examinationId}/series/{serieInstanceUID}/instances", produces = { "application/dicom+json" })
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	ResponseEntity<String> findInstancesOfStudyOfSerie(
			@ApiParam(value = "examinationId", required = true) @PathVariable("examinationId") String examinationId,
			@ApiParam(value = "serieInstanceUID", required = true) @PathVariable("serieInstanceUID") String serieInstanceUID
		) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Returns a frame of a DICOM instance/dataset, of a study and serie", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found instances/datasets", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no instance/dataset found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/studies/{examinationId}/series/{serieInstanceUID}/instances/{sopInstanceUID}/frames/{frame}")
	@PreAuthorize("hasRole('ADMIN') or (hasAnyRole('EXPERT', 'USER') and @datasetSecurityService.hasRightOnExamination(#examinationId, 'CAN_SEE_ALL'))")
	ResponseEntity findFrameOfStudyOfSerieOfInstance(
			@ApiParam(value = "examinationId", required = true) @PathVariable("examinationId") Long examinationId,
			@ApiParam(value = "serieInstanceUID", required = true) @PathVariable("serieInstanceUID") String serieInstanceUID,
			@ApiParam(value = "sopInstanceUID", required = true) @PathVariable("sopInstanceUID") String sopInstanceUID,
			@ApiParam(value = "frame", required = true) @PathVariable("frame") String frame
		) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "Returns all DICOM instances/datasets", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found instances/datasets", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no instance/dataset found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/instances", produces = { "application/dicom+json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<String> findInstances() throws RestServiceException;

	@ApiOperation(value = "", notes = "Returns all DICOM instances/datasets of a study", response = String.class, responseContainer = "List", tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "found instances/datasets", response = String.class, responseContainer = "List"),
			@ApiResponse(code = 204, message = "no instance/dataset found", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@GetMapping(value = "/studies/{studyInstanceUID}/instances", produces = { "application/dicom+json" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<String> findInstancesOfStudy(
			@ApiParam(value = "studyInstanceUID", required = true) @PathVariable("studyInstanceUID") String studyInstanceUID
		) throws RestServiceException;
	
	@ApiOperation(value = "", notes = "STOW-RS", response = Void.class, tags = {})
	@ApiResponses(value = {
			@ApiResponse(code = 204, message = "updated", response = Void.class),
			@ApiResponse(code = 401, message = "unauthorized", response = Void.class),
			@ApiResponse(code = 403, message = "forbidden", response = Void.class),
			@ApiResponse(code = 422, message = "bad parameters", response = ErrorModel.class),
			@ApiResponse(code = 500, message = "unexpected error", response = ErrorModel.class) })
	@PostMapping(value = "/studies", consumes = { "multipart/related" })
	@PreAuthorize("hasAnyRole('ADMIN', 'EXPERT', 'USER')")
	ResponseEntity<Void> stow(
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file
		) throws RestServiceException;


//    @GET
//    @NoCache
//    @Path("/patients/count")
//    @Produces("application/json")
//    public Response countPatients() {
//        return count("CountPatients", Model.PATIENT, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/count")
//    @Produces("application/json")
//    public Response countStudies() {
//        return count("CountStudies", Model.STUDY, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/series/count")
//    @Produces("application/json")
//    public Response countSeries() {
//        return count("CountSeries", Model.SERIES, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/{StudyInstanceUID}/series/count")
//    @Produces("application/json")
//    public Response countSeriesOfStudy(
//            @PathParam("StudyInstanceUID") String studyInstanceUID) {
//        return count("CountStudySeries", Model.SERIES, studyInstanceUID, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/instances/count")
//    @Produces("application/json")
//    public Response countInstances() {
//        return count("CountInstances", Model.INSTANCE, null, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/{StudyInstanceUID}/instances/count")
//    @Produces("application/json")
//    public Response countInstancesOfStudy(
//            @PathParam("StudyInstanceUID") String studyInstanceUID) {
//        return count("CountStudyInstances", Model.INSTANCE, studyInstanceUID, null);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/{StudyInstanceUID}/series/{SeriesInstanceUID}/instances/count")
//    @Produces("application/json")
//    public Response countInstancesOfSeries(
//            @PathParam("StudyInstanceUID") String studyInstanceUID,
//            @PathParam("SeriesInstanceUID") String seriesInstanceUID) {
//        return count("CountStudySeriesInstances", Model.INSTANCE, studyInstanceUID, seriesInstanceUID);
//    }
//
//    @GET
//    @NoCache
//    @Path("/studies/size")
//    @Produces("application/json")

}
