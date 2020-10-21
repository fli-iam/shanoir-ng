package org.shanoir.ng.exporter.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.ApiParam;

@Controller
public class BidsApiController implements BidsApi {
	
	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	private static final String ZIP = ".zip";

	private static final Logger LOG = LoggerFactory.getLogger(BidsApiController.class);

	@Autowired
	BIDSService bidsService;

	@Override
	public ResponseEntity<Void> generateBIDSByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "name of the study", required=true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException {
		bidsService.exportAsBids(studyId, studyName);
		return ResponseEntity.ok().build();
	}

	@Override
    public ResponseEntity<ByteArrayResource> exportBIDSFile(
    		@ApiParam(value = "Id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "file path") @Valid @RequestParam(value = "filePath", required = true) String filePath) throws RestServiceException, IOException {
		// Check filePath too
		// /var/datasets-data/bids-data/stud-1_NATIVE
		if (!filePath.startsWith("/var/datasets-data/bids-data/stud-" + studyId)) {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}

		// Get file, zip it and download it
    	File fileToBeZipped = new File(filePath);
    	if (!fileToBeZipped.exists()) {
			return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    	}
    	
    	// Copy / zip it (and by the way filter only folder that we are interested in)

    	String tmpDir = System.getProperty(JAVA_IO_TMPDIR);
		File zipFile = new File(tmpDir + File.separator + fileToBeZipped.getName() + ZIP);
		zipFile.createNewFile();

		zip(fileToBeZipped.getAbsolutePath(), zipFile.getAbsolutePath());

		byte[] data = Files.readAllBytes(zipFile.toPath());
		ByteArrayResource resource = new ByteArrayResource(data);

		FileUtils.deleteQuietly(zipFile);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + zipFile.getName())
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.contentLength(data.length)
				.body(resource);
	}

	/**
	 * Zip
	 * 
	 * @param sourceDirPath
	 * @param zipFilePath
	 * @throws IOException
	 */
	private void zip(final String sourceDirPath, final String zipFilePath) throws IOException {
		Path p = Paths.get(zipFilePath);
		// 1. Create an outputstream (zip) on the destination
		try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(p))) {
			
			// 2. "Walk" => iterate over the source file
			Path pp = Paths.get(sourceDirPath);
			try(Stream<Path> walker = Files.walk(pp)) {
				
				// 3. We only consider directories, and we copyt them directly by "relativising" them then copying them to the output
				walker.filter(path -> !path.toFile().isDirectory())
				.forEach(path -> {
					ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
					try {
						zos.putNextEntry(zipEntry);
						Files.copy(path, zos);
						zos.closeEntry();
					} catch (IOException e) {
						LOG.error(e.getMessage(), e);
					}
				});
			}
			zos.finish();
		}
	}
}