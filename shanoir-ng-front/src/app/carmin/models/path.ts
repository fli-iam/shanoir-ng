/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2022 Inria - https://www.inria.fr/
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

export interface Path { 
    /**
     * A valid path, slash-separated. It must be consistent with the path of files and directories uploaded and downloaded by clients. For instance, if a user uploads a directory structure \"dir/{file1.txt,file2.txt}\", it is expected that the path of the first file will be \"[prefix]/dir/file1.txt\" and that the path of the second file will be \"[prefix]/dir/file2.txt\" where [prefix] depends on the upload parameters, in particular destination directory.
     */
    platformPath: string;
    /**
     * Date of last modification, in seconds since the Epoch (UNIX timestamp).
     */
    lastModificationDate: number;
    /**
     * True if the path represents a directory.
     */
    isDirectory: boolean;
    /**
     * For a file, size in bytes. For a directory, sum of all the sizes of the files contained in the directory (recursively).
     */
    size?: number;
    /**
     * Id of the Execution that produced the Path.
     */
    executionId?: string;
    /**
     * mime type based on RFC 6838.
     */
    mimeType?: string;
}