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

package org.shanoir.ng.utils;

import java.util.List;

import org.shanoir.ng.shared.exception.ErrorModel;
import org.shanoir.ng.shared.exception.RestServiceException;
import org.shanoir.ng.shared.files.SafeFileNames;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

/**
 * The names of the extra data files of the examinations and dataset acquisitions come from the clients:
 * the names of the uploaded files, and the list of the extra data files of the examination or acquisition.
 * They are used as file names in the storage, so they must not contain any folder.
 */
public final class ExtraDataFileNames {

    private ExtraDataFileNames() {
    }

    /**
     * Checks the extra data sent by a client, before anything is saved.
     *
     * @param extraDataFilePathList the list of the extra data files sent with the examination or acquisition
     * @param files the uploaded files
     * @throws RestServiceException (422) if an uploaded file has no usable name, or if a name of the list
     *         is not a plain file name
     */
    public static void check(List<String> extraDataFilePathList, List<MultipartFile> files) throws RestServiceException {
        for (MultipartFile file : files) {
            try {
                SafeFileNames.fileName(file.getOriginalFilename());
            } catch (IllegalArgumentException e) {
                throw invalidName(file.getOriginalFilename());
            }
        }
        if (extraDataFilePathList != null) {
            for (String name : extraDataFilePathList) {
                if (!isFileName(name)) {
                    throw invalidName(name);
                }
            }
        }
    }

    /**
     * @param files the uploaded files
     * @return the names the files are stored with: only the last part of the names sent by the client
     */
    public static List<String> of(List<MultipartFile> files) {
        return files.stream().map(file -> SafeFileNames.fileName(file.getOriginalFilename())).toList();
    }

    private static boolean isFileName(String name) {
        try {
            return SafeFileNames.fileName(name).equals(name);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static RestServiceException invalidName(String name) {
        return new RestServiceException(new ErrorModel(HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Invalid extra data file name: " + name));
    }

}
