/**
 * 
 */
package org.shanoir.ng.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yyao
 *
 */
public class ImageDicom   {
	  @JsonProperty("imageId")
	  private Integer imageId = null;

	  @JsonProperty("imageFilePath")
	  private String imageFilePath = null;

	}


