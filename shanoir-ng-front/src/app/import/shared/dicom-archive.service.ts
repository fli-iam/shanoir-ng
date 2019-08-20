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

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';


declare var JSZip: any;

@Injectable()
export class DicomArchiveService {

	private fileReader: FileReader = new FileReader();

	constructor() {}

	importFromZip(blob: Blob): Promise<any> {
		this.fileReader.readAsArrayBuffer(blob);
		return new Promise((resolve, reject) => {
			// if success
			this.fileReader.onload = () => {
				resolve(this.fileReader);
			}
			// if failed
			this.fileReader.onerror = error => reject(error);
		});
	}

	clearFileInMemory() {
		this.fileReader = new FileReader();
	}

	extractFileDirectoryStructure(): Promise<any>{
		var zip = new JSZip();
		return zip.loadAsync(this.fileReader.result);
	}
}

