package org.shanoir.uploader.model.dto.rest.importmodel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class ImageDicom {
	
	@JsonProperty("imageId")
	private Integer imageId = null;

	@JsonProperty("imageFilePath")
	private String imageFilePath = null;
}
