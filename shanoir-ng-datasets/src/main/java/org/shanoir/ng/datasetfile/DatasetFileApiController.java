package org.shanoir.ng.datasetfile;

import java.io.File;
import java.time.LocalDateTime;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.datasetfile.service.DatasetFileApi;
import org.shanoir.ng.datasetfile.service.DatasetFileService;
import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.service.DicomServiceApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.ApiParam;

@Controller
public class DatasetFileApiController implements DatasetFileApi {

	@Autowired
	DatasetFileService datasetFileService;

	@Autowired
	@Qualifier("stowrs")
	DicomServiceApi stowRsService;

	@Autowired
	@Qualifier("cstore")
	DicomServiceApi cStoreService;

	@Value("${dcm4chee-arc.protocol}")
	private String dcm4cheeProtocol;

	@Value("${dcm4chee-arc.host}")
	private String dcm4cheeHost;

	@Value("${dcm4chee-arc.port.web}")
	private String dcm4cheePortWeb;

	@Value("${dcm4chee-arc.dicom.web}")
	private boolean dicomWeb;

	@Override
	public 	ResponseEntity<DatasetFile> saveNewDatasetFile(
			@ApiParam(value = "datasetfile to create", required = true) @RequestBody DatasetFile datasetFile,
			BindingResult result) throws RestServiceException  {
		try {
			// Save file
			if (datasetFile.isPacs()) {
				String oldPath = datasetFile.getPath();
				String newPath = oldPath.replaceAll("http(.*)wado\\?", dcm4cheeProtocol + dcm4cheeHost + ":" + dcm4cheePortWeb + "/wado?");
				datasetFile.setPath(newPath);
			}

			DatasetFile createdFile = datasetFileService.create(datasetFile);

			return new ResponseEntity<>(createdFile, HttpStatus.OK);
		} catch (Exception e) {
			throw new RestServiceException(e, new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while adding dataset file."));
		}
	}

	@Override
	public ResponseEntity<Void> addFile(
			@ApiParam(value = "id of the dataset file", required = true) @PathVariable("datasetFileId") Long datasetFileId,
			@ApiParam(value = "file to upload", required = true) @Valid @RequestBody MultipartFile file)
					throws RestServiceException {
		DatasetFile datasetFile = datasetFileService.findById(datasetFileId);
		File destination = null;
		try {
			destination = new File("/tmp/migration-" + datasetFile.getId() + File.separator + file.getName() + LocalDateTime.now());
			if (datasetFile.isPacs()) {
				// Copy file to load it in the PACS
				destination.getParentFile().mkdirs();
				file.transferTo(destination);
				// Transfer to pacs
				if (dicomWeb) {
					stowRsService.sendDicomFilesToPacs(destination.getParentFile());
				} else {
					cStoreService.sendDicomFilesToPacs(destination.getParentFile());
				}
				FileUtils.deleteQuietly(destination.getParentFile());
			} else {
				// Get the dataset file then copy the file to path
				// MOVE nifti (and others) on disc
				destination = new File(datasetFile.getPath().replace("file://", ""));
				destination.getParentFile().mkdirs();
				file.transferTo(destination);
			}

			return new ResponseEntity<>(HttpStatus.OK);
		} catch (Exception e) {
			throw new RestServiceException(e, new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while adding dataset file."));
		} finally {
			if (datasetFile.isPacs()) {
				FileUtils.deleteQuietly(destination);
			}
		}
	}
}
