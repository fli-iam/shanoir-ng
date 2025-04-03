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

package org.shanoir.ng.dataset.dto;

import java.lang.reflect.InvocationTargetException;

import org.shanoir.ng.dataset.model.Dataset;


public class DatasetLight {

	private Long id;
	
	private String name;
	
	private String type;

	private boolean hasProcessings;


    public DatasetLight(Long id, String name, Class<? extends Dataset> type, boolean hasProcessings) throws NoSuchMethodException, InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        this.hasProcessings = hasProcessings;
        this.id = id;
        this.name = name;
        this.type = type.getDeclaredConstructor().newInstance().getType().name();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

    public boolean isHasProcessings() {
        return hasProcessings;
    }

    public void setHasProcessings(boolean hasProcessings) {
        this.hasProcessings = hasProcessings;
    }

}
