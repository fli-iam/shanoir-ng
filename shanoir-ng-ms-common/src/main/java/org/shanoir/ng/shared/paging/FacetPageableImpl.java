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
import org.springframework.data.domain.Sort;

public class FacetPageableImpl implements FacetPageable {
	
	private int pageNumber = 1;
	
	private int pageSize = 20;

	private String filter = null;
	
	private FacetOrder facetOrder = FacetOrder.COUNT;
	
	
	public FacetPageableImpl() {
	}
	
	public FacetPageableImpl(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}
	
	public FacetPageableImpl(int pageNumber, int pageSize, String filter, FacetOrder facetOrder) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
		this.filter = filter;
		this.facetOrder = facetOrder;
	}	

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public long getOffset() {
		return pageNumber * pageSize;
	}

	@Override
	public Sort getSort() {
		return Sort.unsorted(); // No use for sort ? (Solr don't implements sorting for facets except for INDEX/COUNT)
	}

	@Override
	public Pageable next() {
		return new FacetPageableImpl(pageNumber + 1, pageSize, filter, facetOrder);
	}

	@Override
	public Pageable previousOrFirst() {
		if (pageNumber == 1) return new FacetPageableImpl(pageNumber, pageSize, filter, facetOrder);
		else return new FacetPageableImpl(pageNumber - 1, pageSize, filter, facetOrder);
	}

	@Override
	public Pageable first() {
		return new FacetPageableImpl(1, pageSize, filter, facetOrder);
	}

	@Override
	public boolean hasPrevious() {
		return pageNumber > 1;
	}

	@Override
	public String getFilter() {
		return filter;
	}

	@Override
	public FacetOrder getFacetOrder() {
		return facetOrder;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public void setFacetOrder(FacetOrder facetOrder) {
		this.facetOrder = facetOrder;
	}	
	
	public void setFacetOrder(String facetOrder) {
		try {
			this.facetOrder = FacetOrder.valueOf(facetOrder);			
		} catch (IllegalArgumentException e) {}
	}
}