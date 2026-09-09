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

public class PageImpl<T> extends org.springframework.data.domain.PageImpl<T> implements Page<T> {

    private static final long serialVersionUID = 1L;

    /**
     * The superclass's {@link #getContent()} always returns an unmodifiable view of its content list,
     * so this class keeps its own mutable copy to actually support the {@link Collection} mutator
     * methods (add, remove, ...) that {@link Page} exposes.
     */
    private final List<T> mutableContent;

    public PageImpl() {
        super(new ArrayList<T>());
        this.mutableContent = new ArrayList<>();
    }

    public PageImpl(org.springframework.data.domain.Page<T> page) {
        super(page.getContent(), PageRequest.of(page.getNumber(), page.getSize(), page.getSort()), page.getTotalElements());
        this.mutableContent = new ArrayList<>(page.getContent());
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
        this.mutableContent = new ArrayList<>(content);
    }

    /**
     * Creates a new {@link PageImpl} with the given content. This will result in the created {@link Page} being identical
     * to the entire {@link List}.
     *
     * @param content must not be {@literal null}.
     */
    public PageImpl(List<T> content) {
        super(content);
        this.mutableContent = new ArrayList<>(content);
    }

    @Override
    public List<T> getContent() {
        return mutableContent;
    }

    @Override
    public boolean add(T e) {
        return this.mutableContent.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return this.mutableContent.addAll(c);
    }

    @Override
    public void clear() {
        this.mutableContent.clear();
    }

    @Override
    public boolean contains(Object o) {
        return this.mutableContent.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.mutableContent.containsAll(c);
    }

    @Override
    public boolean isEmpty() {
        return this.mutableContent.isEmpty();
    }

    @Override
    public boolean remove(Object o) {
        return this.mutableContent.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.mutableContent.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.mutableContent.retainAll(c);
    }

    @Override
    public int size() {
        return this.mutableContent.size();
    }

    @Override
    public Object[] toArray() {
        return this.mutableContent.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.mutableContent.toArray(a);
    }

}
