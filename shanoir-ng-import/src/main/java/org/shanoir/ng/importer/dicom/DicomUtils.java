package org.shanoir.ng.importer.dicom;

import java.io.File;
import java.io.FileNotFoundException;

public class DicomUtils {

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
