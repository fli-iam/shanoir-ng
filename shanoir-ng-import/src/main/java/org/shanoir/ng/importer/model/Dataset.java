package org.shanoir.ng.importer.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dataset {
	
	@JsonProperty("name")
	private String name;
	
	private List<ExpressionFormat> expressionFormats = new ArrayList<ExpressionFormat>();

	@JsonProperty("diffusionGradients")
	private List<DiffusionGradient> diffusionGradients;
	
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
			diffusionGradients = new ArrayList<DiffusionGradient>();
		}
		return diffusionGradients;
	}

	public void setDiffusionGradients(List<DiffusionGradient> diffusionGradients) {
		this.diffusionGradients = diffusionGradients;
	}

}
