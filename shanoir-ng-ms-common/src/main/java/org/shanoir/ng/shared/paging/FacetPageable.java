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

package org.shanoir.ng.shared.paging;

import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as=FacetPageableImpl.class)
public interface FacetPageable extends Pageable {

	/**
	 * Get the filtering term for this paging configuration
	 */
	public String getFilter();
	
	/**
	 * Get the facet order, can only be COUNT or INDEX
	 */
	public FacetOrder getFacetOrder();

	
	enum FacetOrder {
		
		COUNT("COUNT"),
		INDEX("INDEX");
		
		private final String value;
				
		FacetOrder(String str) {
			this.value = str;
		}
		
		@Override
		public String toString() {
			return value;
		}
		
	}
	
}