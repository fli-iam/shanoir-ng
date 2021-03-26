package org.shanoir.ng.exporter.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.shanoir.ng.exporter.service.BIDSService;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.utils.KeycloakUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

	private final HttpServletRequest request;

	@org.springframework.beans.factory.annotation.Autowired
	public BidsApiController(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public ResponseEntity<Void> generateBIDSByStudyId(
    		@ApiParam(value = "id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "name of the study", required=true) @PathVariable("studyName") String studyName) throws RestServiceException, IOException {
		bidsService.exportAsBids(studyId, studyName);
		return ResponseEntity.ok().build();
	}

	@Override
    public void exportBIDSFile(
    		@ApiParam(value = "Id of the study", required=true) @PathVariable("studyId") Long studyId,
    		@ApiParam(value = "file path") @Valid @RequestParam(value = "filePath", required = true) String filePath, HttpServletResponse response) throws RestServiceException, IOException {
		// Check filePath too
		// /var/datasets-data/bids-data/stud-1_NATIVE
		if (!filePath.startsWith("/var/datasets-data/bids-data/stud-" + studyId)) {
			response.sendError(HttpStatus.UNAUTHORIZED.value());
			return;
		}

		// Get file, zip it and download it
    	File fileToBeZipped = new File(filePath);
    	if (!fileToBeZipped.exists()) {
			response.sendError(HttpStatus.NO_CONTENT.value());
			return;
    	}
    	
    	// Copy / zip it (and by the way filter only folder that we are interested in)
    	String userDir = getUserDir(System.getProperty(JAVA_IO_TMPDIR)).getAbsolutePath();

		// Add timestamp to get a "random" difference
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		File tmpFile = new File(userDir + File.separator + formatter.format(new DateTime().toDate()) + File.separator);
		tmpFile.mkdirs();
		File zipFile = new File(tmpFile.getAbsolutePath() + File.separator + fileToBeZipped.getName() + ZIP);
		zipFile.createNewFile();

		zip(fileToBeZipped.getAbsolutePath(), zipFile.getAbsolutePath());
		
		// Try to determine file's content type
		String contentType = request.getServletContext().getMimeType(zipFile.getAbsolutePath());

		try (InputStream is = new FileInputStream(zipFile);) {
			response.setHeader("Content-Disposition", "attachment;filename=" + zipFile.getName());
			response.setContentType(contentType);
			org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} finally {
			FileUtils.deleteQuietly(zipFile);
		}
	}

	public static File getUserDir(String importDir) {
		final Long userId = KeycloakUtil.getTokenUserId();
		final String userImportDirFilePath = importDir + File.separator + Long.toString(userId);
		final File userImportDir = new File(userImportDirFilePath);
		if (!userImportDir.exists()) {
			userImportDir.mkdirs(); // create if not yet existing
		} // else is wanted case, user has already its import directory
		return userImportDir;
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