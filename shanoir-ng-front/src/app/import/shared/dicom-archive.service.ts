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

	importFromZip(blob: Blob): Observable<any> {
		this.fileReader.readAsArrayBuffer(blob);
		return Observable.create(observer => {
			// if success
			this.fileReader.onload = () => {
				observer.next(this.fileReader);
			}
			// if failed
			this.fileReader.onerror = error => observer.error(error);
		});
	}

	clearFileInMemory() {
		this.fileReader = new FileReader();
	}

	extractFileDirectoryStructure(): Observable<any>{
		return Observable.create(observer => {
			var zip = new JSZip();
			zip.loadAsync(this.fileReader.result).then(function (x) {
				observer.next(x);
			}).catch((error) => {});
		});
	}
}

