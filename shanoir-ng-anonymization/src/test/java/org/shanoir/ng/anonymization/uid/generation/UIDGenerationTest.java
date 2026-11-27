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

package org.shanoir.ng.anonymization.uid.generation;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

public class UIDGenerationTest {

    private static final int UID_MAX_LENGTH = 64;

    /** Components of digits, none starting with 0 unless it is a single 0 (DICOM PS3.5, chapter 9). */
    private static final Pattern VALID_UID = Pattern.compile("(0|[1-9][0-9]*)(\\.(0|[1-9][0-9]*))*");

    @Test
    public void newUidsAreValidDicomUids() {
        UIDGeneration generator = new UIDGeneration();
        for (int i = 0; i < 5000; i++) {
            String uid = generator.getNewUID();
            Assert.assertTrue("not under the Shanoir root: " + uid, uid.startsWith(UIDGeneration.ROOT + "."));
            Assert.assertTrue("longer than 64 characters: " + uid, uid.length() <= UID_MAX_LENGTH);
            Assert.assertTrue("not a valid DICOM UID: " + uid, VALID_UID.matcher(uid).matches());
        }
    }

    @Test
    public void newUidsAreUnique() {
        UIDGeneration generator = new UIDGeneration();
        Set<String> uids = new HashSet<>();
        for (int i = 0; i < 20000; i++) {
            Assert.assertTrue("duplicate UID", uids.add(generator.getNewUID()));
        }
    }

}
