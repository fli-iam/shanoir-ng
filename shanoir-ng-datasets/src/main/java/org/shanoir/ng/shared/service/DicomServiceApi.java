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

package org.shanoir.ng.shared.service;

import java.io.File;

import org.shanoir.ng.shared.exception.ShanoirException;

public interface DicomServiceApi {
	
	void sendDicomFilesToPacs(File directoryWithDicomFiles) throws Exception;

	/**
	 * Delete associated files from the PACS
	 * @param url the dataset file URL linked to the dataset
	 * @throws Exception when the deletion fails
	 */
	void deleteDicomFilesFromPacs(String url) throws ShanoirException;
}