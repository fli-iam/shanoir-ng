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
	FINISHED, //
	ERROR; //

	public static UploadState fromString(String value) {
		// Handle modification of FINISHED_UPLOAD state to FINISHED
		if (value.equalsIgnoreCase("FINISHED_UPLOAD")) {
			return UploadState.FINISHED;
		}
        for (UploadState state : UploadState.values()) {
            if (state.name().equalsIgnoreCase(value)) {
                return state;
            }
        }
        throw new IllegalArgumentException("No UploadState value corresponding to: " + value);
    }
}
