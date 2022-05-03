package org.shanoir.uploader.upload;

import javax.xml.bind.annotation.XmlType;

/**
 * This class contains all states of the UploadService's
 * state engine, which is responsible to assure a secure
 * upload to the Shanoir server.
 * @author mkain
 *
 */
@XmlType
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
