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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageImpl<T> extends org.springframework.data.domain.PageImpl<T> implements Page<T>{

	private static final long serialVersionUID = 1L;


	public PageImpl() {
		super(new ArrayList<T>());
	}

	public PageImpl(org.springframework.data.domain.Page<T> page) {
		super(page.getContent(), PageRequest.of(page.getNumber(), page.getSize(), page.getSort()), page.getTotalElements());
	}


    /**
	 * Constructor of {@code PageImpl}.
	 * 
	 * @param content the content of this page, must not be {@literal null}.
	 * @param pageable the paging information, can be {@literal null}.
	 * @param total the total amount of items available. The total might be adapted considering the length of the content
	 *          given, if it is going to be the content of the last page. This is in place to mitigate inconsistencies
	 */
	public PageImpl(List<T> content, Pageable pageable, long total) {
		super(content, pageable, total);
	}

	/**
	 * Creates a new {@link PageImpl} with the given content. This will result in the created {@link Page} being identical
	 * to the entire {@link List}.
	 * 
	 * @param content must not be {@literal null}.
	 */
	public PageImpl(List<T> content) {
		super(content);
    }
    
    @Override
	public boolean add(T e) {
		return this.getContent().add(e);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		return this.getContent().addAll(c);
	}

	@Override
	public void clear() {
		this.getContent().clear();
	}

	@Override
	public boolean contains(Object o) {
		return this.getContent().contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.getContent().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return this.getContent().isEmpty();
	}

	@Override
	public boolean remove(Object o) {
		return this.getContent().remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
        return this.getContent().removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
        return this.getContent().retainAll(c);
	}

	@Override
	public int size() {
		return this.getContent().size();
	}

	@Override
	public Object[] toArray() {
		return this.getContent().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.getContent().toArray(a);
	}

}