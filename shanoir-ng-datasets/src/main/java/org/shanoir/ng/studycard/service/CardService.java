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

package org.shanoir.ng.studycard.service;

import org.shanoir.ng.shared.exception.EntityNotFoundException;
import org.shanoir.ng.shared.exception.MicroServiceCommunicationException;
import org.shanoir.ng.studycard.model.Card;

import java.util.List;

// MK: had to move down all sec annotations (Pre-/PostAuthorize) to -ServiceImpls as Spring Sec throws a duplicate
// annotation exception, because of 2 interface hierarchy, that is not well managed by Spring Sec, so I could not
// keep the annotations here.
public interface CardService<T extends Card> {

    void deleteById(Long id) throws EntityNotFoundException, MicroServiceCommunicationException;

    T save(T card) throws MicroServiceCommunicationException;

    T update(T card) throws EntityNotFoundException, MicroServiceCommunicationException;

    List<T> findAll();

    T findById(Long id);

    T findByName(String name);

    List<T> search(List<Long> studyIdList);

    List<T> findByStudy(Long studyId);

}
