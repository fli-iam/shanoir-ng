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

package org.shanoir.ng.shared.files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SafeFileNamesTest {

    @TempDir
    private Path folder;

    @Test
    void fileNameKeepsOnlyTheLastPartOfTheName() {
        assertEquals("data.txt", SafeFileNames.fileName("data.txt"));
        assertEquals("evil.txt", SafeFileNames.fileName("../../evil.txt"));
        assertEquals("evil.txt", SafeFileNames.fileName("/etc/evil.txt"));
        assertEquals("evil.txt", SafeFileNames.fileName("..\\..\\evil.txt"));
        assertEquals("data.txt", SafeFileNames.fileName("C:\\Users\\me\\data.txt"));
        assertEquals("..data", SafeFileNames.fileName("..data"));
    }

    @Test
    void fileNameRefusesTheNamesWithoutAFile() {
        assertThrows(IllegalArgumentException.class, () -> SafeFileNames.fileName(null));
        assertThrows(IllegalArgumentException.class, () -> SafeFileNames.fileName(""));
        assertThrows(IllegalArgumentException.class, () -> SafeFileNames.fileName("."));
        assertThrows(IllegalArgumentException.class, () -> SafeFileNames.fileName(".."));
        assertThrows(IllegalArgumentException.class, () -> SafeFileNames.fileName("folder/"));
        assertThrows(IllegalArgumentException.class, () -> SafeFileNames.fileName("a/.."));
        assertThrows(IllegalArgumentException.class, () -> SafeFileNames.fileName("data\0.txt"));
    }

    @Test
    void resolveInsideKeepsThePathsInsideTheFolder() throws IOException {
        assertEquals(folder.resolve("data.txt"), SafeFileNames.resolveInside(folder, "data.txt"));
        assertEquals(folder.resolve("sub/data.txt"), SafeFileNames.resolveInside(folder, "sub/data.txt"));
        assertEquals(folder.resolve("data.txt"), SafeFileNames.resolveInside(folder, "sub/../data.txt"));
    }

    @Test
    void resolveInsideRefusesThePathsLeavingTheFolder() {
        assertThrows(IOException.class, () -> SafeFileNames.resolveInside(folder, "../evil.txt"));
        assertThrows(IOException.class, () -> SafeFileNames.resolveInside(folder, "sub/../../evil.txt"));
        assertThrows(IOException.class, () -> SafeFileNames.resolveInside(folder, "/etc/evil.txt"));
        assertThrows(IOException.class, () -> SafeFileNames.resolveInside(folder, ".."));
        assertThrows(IOException.class, () -> SafeFileNames.resolveInside(folder, ""));
    }

}
