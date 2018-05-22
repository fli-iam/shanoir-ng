package org.shanoir.ng.preclinical.pathologies.pathology_models;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.shanoir.ng.configuration.ShanoirPreclinicalConfiguration;
import org.shanoir.ng.preclinical.pathologies.Pathology;
import org.shanoir.ng.preclinical.pathologies.PathologyService;
import org.shanoir.ng.shared.error.FieldErrorMap;
import org.shanoir.ng.shared.exception.ErrorDetails;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.exception.ShanoirPreclinicalException;
import org.shanoir.ng.shared.validation.EditableOnlyByValidator;
import org.shanoir.ng.shared.validation.UniqueValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiParam;

@Controller
public class PathologyModelApiController implements PathologyModelApi {

	private static final Logger LOG = LoggerFactory.getLogger(PathologyModelApiController.class);

	@Autowired
	private PathologyModelService modelsService;
	@Autowired
	private PathologyService pathologiesService;
	@Autowired
	private ShanoirPreclinicalConfiguration preclinicalConfig;

	public ResponseEntity<PathologyModel> createPathologyModel(
			@ApiParam(value = "pathology model to create", required = true) @RequestBody PathologyModel model,
			BindingResult result) throws RestServiceException {

		final FieldErrorMap accessErrors = this.getCreationRightsErrors(model);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(model);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		// Guarantees it is a creation, not an update
		model.setId(null);

		/* Save model in db. */
		try {
			final PathologyModel createdModel = modelsService.save(model);
			return new ResponseEntity<PathologyModel>(createdModel, HttpStatus.OK);
		} catch (ShanoirPreclinicalException e) {
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}

	}

	public ResponseEntity<Void> deletePathologyModel(
			@ApiParam(value = "Pathology model id to delete", required = true) @PathVariable("id") Long id) {
		PathologyModel toDelete = modelsService.findById(id);
		if (toDelete == null) {
			return new ResponseEntity<Void>(HttpStatus.NOT_FOUND);
		}
		try {
			// Find and delete corresponding file
			if (Files.exists(Paths.get(toDelete.getFilepath())))
				Files.delete(Paths.get(toDelete.getFilepath()));
		} catch (Exception e) {
			LOG.error("There was an error trying to delete files from " + toDelete.getFilepath()
					+ toDelete.getFilename() + " " + e.getMessage());
		}
		try {
			modelsService.deleteById(id);
		} catch (ShanoirPreclinicalException e) {
			return new ResponseEntity<Void>(HttpStatus.NOT_ACCEPTABLE);
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}

	public ResponseEntity<PathologyModel> getPathologyModelById(
			@ApiParam(value = "ID of pathology model that needs to be fetched", required = true) @PathVariable("id") Long id) {
		final PathologyModel model = modelsService.findById(id);
		if (model == null) {
			return new ResponseEntity<PathologyModel>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<PathologyModel>(model, HttpStatus.OK);
	}

	public ResponseEntity<List<PathologyModel>> getPathologyModels() {
		final List<PathologyModel> models = modelsService.findAll();
		if (models.isEmpty()) {
			return new ResponseEntity<List<PathologyModel>>(HttpStatus.NO_CONTENT);
		}
		return new ResponseEntity<List<PathologyModel>>(models, HttpStatus.OK);
	}

	public ResponseEntity<List<PathologyModel>> getPathologyModelsByPathology(
			@ApiParam(value = "ID of pathology", required = true) @PathVariable("id") Long id) {
		Pathology pathology = pathologiesService.findById(id);
		if (pathology == null) {
			return new ResponseEntity<List<PathologyModel>>(HttpStatus.NOT_FOUND);
		} else {
			final List<PathologyModel> models = modelsService.findByPathology(pathology);
			if (models.isEmpty()) {
				return new ResponseEntity<List<PathologyModel>>(HttpStatus.NO_CONTENT);
			}
			return new ResponseEntity<List<PathologyModel>>(models, HttpStatus.OK);
		}
	}

	public ResponseEntity<Void> updatePathologyModel(
			@ApiParam(value = "ID of pathology model that needs to be updated", required = true) @PathVariable("id") Long id,
			@ApiParam(value = "Pathology model object that needs to be updated", required = true) @RequestBody PathologyModel model,
			final BindingResult result) throws RestServiceException {

		model.setId(id);

		final FieldErrorMap accessErrors = this.getUpdateRightsErrors(model);
		final FieldErrorMap hibernateErrors = new FieldErrorMap(result);
		final FieldErrorMap uniqueErrors = this.getUniqueConstraintErrors(model);
		/* Merge errors. */
		final FieldErrorMap errors = new FieldErrorMap(accessErrors, hibernateErrors, uniqueErrors);
		if (!errors.isEmpty()) {
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", new ErrorDetails(errors)));
		}

		try {
			modelsService.update(model);
		} catch (ShanoirPreclinicalException e) {
			LOG.error("Error while trying to update pathology model" + id + " : ", e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad arguments", null));
		}
		return new ResponseEntity<Void>(HttpStatus.OK);

	}

	public ResponseEntity<PathologyModel> uploadModelSpecifications(
			@ApiParam(value = "ID of pathology model upload data to", required = true) @PathVariable("id") Long id,
			@RequestParam("files") MultipartFile[] uploadfiles) throws RestServiceException {

		if (uploadfiles == null || uploadfiles.length == 0) {
			LOG.error("uploadFiles is null or empty " + (uploadfiles == null ? "null" : "empty"));
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "No file uploaded", null));

		}
		if (id == null) {
			LOG.error("Error while uploadModelSpecifications: pathology model id is null");
			throw new RestServiceException(
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Bad Arguments", null));
		}

		PathologyModel model = modelsService.findById(id);
		try {
			model = saveUploadedFile(model, uploadfiles[0]);
			modelsService.save(model);
			return new ResponseEntity<PathologyModel>(model, HttpStatus.OK);
		} catch (IOException e) {
			LOG.error("Error while uploadModelSpecifications: issue with file " + (e == null ? "" : e.getMessage()), e);
			throw new RestServiceException(e,
					new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(), "Error while saving uploaded file", null));
		} catch (ShanoirPreclinicalException e) {
			LOG.error("Error while uploadModelSpecifications: saving in db " + (e == null ? "" : e.getMessage()), e);
			throw new RestServiceException(e, new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
					"Error while saving updated model specifications", null));
		}
	}

	public ResponseEntity<Resource> downloadModelSpecifications(
			@ApiParam(value = "ID of model specifications file to download", required = true) @PathVariable("id") Long id)
			throws RestServiceException {

		final PathologyModel model = modelsService.findById(id);
		if (model != null) {
			try {
				File toDownload = new File(model.getFilepath());
				Path path = Paths.get(toDownload.getAbsolutePath());
				ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(path));

				HttpHeaders header = new HttpHeaders();
				header.setContentType(MediaType.APPLICATION_PDF);
				header.set(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=" + model.getFilename().replace(" ", "_"));

				return ResponseEntity.ok().headers(header).contentLength(toDownload.length())
						.contentType(MediaType.parseMediaType("application/octet-stream")).body((Resource) resource);
			} catch (IOException ioe) {
				LOG.error("Error while getting file to download " + ioe.getMessage());
				return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
			}
		}
		return new ResponseEntity<Resource>(HttpStatus.NO_CONTENT);
	}

	private FieldErrorMap getUpdateRightsErrors(final PathologyModel model) {
		final PathologyModel previousStateModel = modelsService.findById(model.getId());
		final FieldErrorMap accessErrors = new EditableOnlyByValidator<PathologyModel>().validate(previousStateModel,
				model);
		return accessErrors;
	}

	private FieldErrorMap getCreationRightsErrors(final PathologyModel model) {
		return new EditableOnlyByValidator<PathologyModel>().validate(model);
	}

	private FieldErrorMap getUniqueConstraintErrors(final PathologyModel model) {
		final UniqueValidator<PathologyModel> uniqueValidator = new UniqueValidator<PathologyModel>(modelsService);
		final FieldErrorMap uniqueErrors = uniqueValidator.validate(model);
		return uniqueErrors;
	}

	private PathologyModel saveUploadedFile(PathologyModel model, MultipartFile file) throws IOException {
		// Create corresponding folders
		Path path = Paths.get(preclinicalConfig.getUploadExtradataFolder() + "models/" + model.getId());
		Files.createDirectories(path);
		// Path to file
		Path pathToFile = Paths.get(path.toString() + "/" + file.getOriginalFilename());
		byte[] bytes = file.getBytes();
		// Path path = Paths.get(UPLOADED_EXAM_FOLDER + file.getOriginalFilename());
		Files.write(pathToFile, bytes);
		model.setFilename(file.getOriginalFilename());
		model.setFilepath(pathToFile.toString());
		return model;
	}

}
