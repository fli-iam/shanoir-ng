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

package org.shanoir.ng.subject.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.subject.service.SubjectNamePatternUtils.NameBase;

/**
 * Patterns below have the shapes produced by study.component.ts buildSubjectNamePatternRegex().
 */
class SubjectNamePatternUtilsTest {

    private static String firstName(NameBase nameBase) {
        return nameBase.base() + "1".repeat(nameBase.idLength());
    }

    @Test
    void studyNamePrefix() {
        String pattern = "^My\\.Study-[0-9]{4}$";
        NameBase nameBase = SubjectNamePatternUtils.computeNameBase(pattern, null);
        assertEquals("My.Study-", nameBase.base());
        assertEquals(4, nameBase.idLength());
        assertTrue(firstName(nameBase).matches(pattern));
    }

    @Test
    void studyNameWithEscapedParenthesesIsNotACenterSegment() {
        String pattern = "^Study \\(2\\)-[0-9]{2}$";
        NameBase nameBase = SubjectNamePatternUtils.computeNameBase(pattern, "01");
        assertEquals("Study (2)-", nameBase.base());
        assertTrue(firstName(nameBase).matches(pattern));
    }

    @Test
    void customNamesUseTheFirstOne() {
        String pattern = "^(ABC|XYZ)_[A-Za-z0-9]{3}$";
        NameBase nameBase = SubjectNamePatternUtils.computeNameBase(pattern, null);
        assertEquals("ABC_", nameBase.base());
        assertTrue(firstName(nameBase).matches(pattern));
    }

    @Test
    void mandatoryCenterSegmentWithListedCenter() {
        String pattern = "^ABC-(01|02)-[0-9]{4}$";
        NameBase nameBase = SubjectNamePatternUtils.computeNameBase(pattern, "02");
        assertEquals("ABC-02-", nameBase.base());
        assertTrue(firstName(nameBase).matches(pattern));
    }

    @Test
    void mandatoryCenterSegmentWithoutListedCenterCannotMatch() {
        String pattern = "^ABC-(01|02)-[0-9]{4}$";
        NameBase nameBase = SubjectNamePatternUtils.computeNameBase(pattern, "03");
        assertEquals("ABC-", nameBase.base());
        assertFalse(firstName(nameBase).matches(pattern));
    }

    @Test
    void optionalCenterSegmentIsOmittedWithoutCenterIndex() {
        String pattern = "^ABC(-(01|02))?-[0-9]{4}$";
        NameBase withoutIndex = SubjectNamePatternUtils.computeNameBase(pattern, null);
        assertEquals("ABC-", withoutIndex.base());
        assertTrue(firstName(withoutIndex).matches(pattern));
        NameBase withIndex = SubjectNamePatternUtils.computeNameBase(pattern, "01");
        assertEquals("ABC-01-", withIndex.base());
        assertTrue(firstName(withIndex).matches(pattern));
    }

    @Test
    void customNamesWithCenterSegment() {
        String pattern = "^(A|B)\\.(01)\\.[0-9]{2}$";
        NameBase nameBase = SubjectNamePatternUtils.computeNameBase(pattern, "01");
        assertEquals("A.01.", nameBase.base());
        assertTrue(firstName(nameBase).matches(pattern));
    }

    @Test
    void unsupportedShapeGivesNull() {
        assertNull(SubjectNamePatternUtils.computeNameBase("^[a-z]+$", null));
        assertNull(SubjectNamePatternUtils.computeNameBase(null, null));
    }
}
