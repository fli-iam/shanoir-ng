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

package org.shanoir.ng.examination.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.shared.event.ShanoirEvent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Guards against regressions where destructive service methods are accidentally
 * marked {@code @Transactional(readOnly = true)} (see #3520 / NG_v2.14.0 subject deletion bug).
 */
class ExaminationServiceImplTransactionalTest {

    @Test
    void deleteByIdMustNotUseReadOnlyTransaction() throws NoSuchMethodException {
        Method method = ExaminationServiceImpl.class.getDeclaredMethod("deleteById", Long.class, ShanoirEvent.class);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional, "deleteById must be transactional so DELETE statements persist");
        assertFalse(transactional.readOnly(),
                "deleteById performs DELETE operations and must not use a read-only transaction");
    }

    @Test
    void findPageMayUseReadOnlyTransaction() throws NoSuchMethodException {
        Method method = ExaminationServiceImpl.class.getDeclaredMethod(
                "findPage",
                org.springframework.data.domain.Pageable.class,
                boolean.class,
                String.class,
                String.class);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertTrue(transactional.readOnly(), "findPage is read-only by design");
    }
}
