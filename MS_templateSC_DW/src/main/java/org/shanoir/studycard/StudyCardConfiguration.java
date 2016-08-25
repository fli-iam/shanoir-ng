package org.shanoir.studycard;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

/**
 * Project configuration class.
 * 
 * @author msimon
 *
 */
public class StudyCardConfiguration extends Configuration {

	@Valid
	@NotNull
	public DataSourceFactory dataSourceFactory;

	/**
	 * @return the database
	 */
	@JsonProperty("database")
	public DataSourceFactory getDataSourceFactory() {
		return dataSourceFactory;
	}

}
