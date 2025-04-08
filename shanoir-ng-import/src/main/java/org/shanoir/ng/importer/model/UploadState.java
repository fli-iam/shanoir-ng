package org.shanoir.ng.importer.model;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * This class contains all states of upload process coming from Shanoir Uploader
 * @author mkain
 *
 */
@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum UploadState {
	START, //
	START_AUTOIMPORT,
	START_AUTOIMPORT_FAIL,
	MISSING, // Is it used ?
	READY, // Is it used ?
	UPLOADING_IMAGES, // Is it used ?
	UPLOADING_JOB_FILE, // Is it used ?
	FINISHED_UPLOAD, //
	ERROR //
}
