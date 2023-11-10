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

package org.shanoir.ng.download;

import java.util.ArrayList;
import java.util.List;

import org.dcm4che3.data.Attributes;

public class DatasetAttributes {

	private List<Attributes> images = new ArrayList<>();

	public List<Attributes> getAllImageAttributes() {
		return images;
	}

	public void addImageAttributes(Attributes attributes) {
		this.images.add(attributes);
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Attributes image : images) {
            sb.append("image \n");
            for(String line : image.toString(1000, 1000).split("\n")) {
                sb.append("\t").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public Attributes getFirstImageAttributes() {
        if (images != null && images.size() > 0) {
			return images.get(0);
		} else {
			return null;
		}
    }
}