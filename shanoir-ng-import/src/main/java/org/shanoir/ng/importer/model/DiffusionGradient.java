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


public class DiffusionGradient {

	private Double diffusionGradientBValue;

	private Double diffusionGradientOrientationX;

	private Double diffusionGradientOrientationY;

	private Double diffusionGradientOrientationZ;

	public Double getDiffusionGradientBValue() {
		return diffusionGradientBValue;
	}

	public void setDiffusionGradientBValue(Double diffusionGradientBValue) {
		this.diffusionGradientBValue = diffusionGradientBValue;
	}

	public Double getDiffusionGradientOrientationX() {
		return diffusionGradientOrientationX;
	}

	public void setDiffusionGradientOrientationX(Double diffusionGradientOrientationX) {
		this.diffusionGradientOrientationX = diffusionGradientOrientationX;
	}

	public Double getDiffusionGradientOrientationY() {
		return diffusionGradientOrientationY;
	}

	public void setDiffusionGradientOrientationY(Double diffusionGradientOrientationY) {
		this.diffusionGradientOrientationY = diffusionGradientOrientationY;
	}

	public Double getDiffusionGradientOrientationZ() {
		return diffusionGradientOrientationZ;
	}

	public void setDiffusionGradientOrientationZ(Double diffusionGradientOrientationZ) {
		this.diffusionGradientOrientationZ = diffusionGradientOrientationZ;
	}


}
