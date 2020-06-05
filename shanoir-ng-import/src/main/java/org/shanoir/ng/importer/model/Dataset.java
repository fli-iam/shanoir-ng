/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dataset {
	
	@JsonProperty("name")
	private String name;
	
	private List<ExpressionFormat> expressionFormats = new ArrayList<>();

	@JsonProperty("diffusionGradients")
	private List<DiffusionGradient> diffusionGradients;
	
	@JsonProperty("repetitionTimes")
	public Set<Double> repetitionTimes;
	
	@JsonProperty("inversionTimes")
	public Set<Double> inversionTimes;

	@JsonProperty("echoTimes")
	public Set<EchoTime> echoTimes;
	
	@JsonProperty("flipAngles")
	public Set<Double> flipAngles;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ExpressionFormat> getExpressionFormats() {
		return expressionFormats;
	}

	public void setExpressionFormats(List<ExpressionFormat> expressionFormats) {
		this.expressionFormats = expressionFormats;
	}

	public List<DiffusionGradient> getDiffusionGradients() {
		if (diffusionGradients == null) {
			diffusionGradients = new ArrayList<>();
		}
		return diffusionGradients;
	}

	public void setDiffusionGradients(List<DiffusionGradient> diffusionGradients) {
		this.diffusionGradients = diffusionGradients;
	}

	public Set<Double> getRepetitionTimes() {
		if (repetitionTimes == null) {
			this.repetitionTimes = new HashSet<>();
		}
		return this.repetitionTimes;
	}

	public void setRepetitionTimes(Set<Double> repetitionTimes) {
		this.repetitionTimes = repetitionTimes;
	}

	public Set<Double> getInversionTimes() {
		if (inversionTimes == null) {
			this.inversionTimes = new HashSet<>();
		}
		return this.inversionTimes;
	}

	public void setInversionTimes(Set<Double> inversionTimes) {
		this.inversionTimes = inversionTimes;
	}

	public Set<Double> getFlipAngles() {
		if (flipAngles == null) {
			this.flipAngles = new HashSet<>();
		}
		return this.flipAngles;
	}

	public void setFlipAngles(Set<Double> flipAngles) {
		this.flipAngles = flipAngles;
	}

	public Set<EchoTime> getEchoTimes() {
		if (echoTimes == null) {
			this.echoTimes =  new HashSet<>();
		}
		return this.echoTimes;
	}

	public void setEchoTimes(Set<EchoTime> echoTimes) {
		this.echoTimes = echoTimes;
	}



}
