package org.shanoir.ng.importer.dto;

import java.util.ArrayList;
import java.util.List;
import org.shanoir.ng.shared.model.DiffusionGradient;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Dataset {
	
	@JsonProperty("name")
	private String name;
	
	private List<ExpressionFormat> expressionFormats;
	
	@JsonProperty("diffusionGradients")
	private List<DiffusionGradient> diffusionGradients;

	@JsonProperty("bValues")
	private List<Double> bValues;
	
	@JsonProperty("bVectors")
	private List<Double> bVectors;
	
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
	
	public List<Double> getbValues() {
		return bValues;
	}

	public void setbValues(List<Double> bValues) {
		this.bValues = bValues;
	}

	public List<Double> getbVectors() {
		return bVectors;
	}

	public void setbVectors(List<Double> bVectors) {
		this.bVectors = bVectors;
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
