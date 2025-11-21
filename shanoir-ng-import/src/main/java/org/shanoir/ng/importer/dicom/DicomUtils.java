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

package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.FileNotFoundException;

public final class DicomUtils {

    private DicomUtils() { }

    public static String referencedFileIDToPath(String rootFilePath, String[] referencedFileIDArray) throws FileNotFoundException {
        StringBuilder stringBuilder = new StringBuilder();
        if (referencedFileIDArray != null) {
            stringBuilder.append(rootFilePath).append(File.separator);
            for (int count = 0; count < referencedFileIDArray.length; count++) {
                stringBuilder.append(referencedFileIDArray[count]);
                if (count != referencedFileIDArray.length - 1) {
                    stringBuilder.append(File.separator);
                }
            }
            return stringBuilder.toString();
        } else {
            throw new FileNotFoundException(
                    "instancePathArray in DicomDir: missing file: " + referencedFileIDArray);
        }
    }

}
