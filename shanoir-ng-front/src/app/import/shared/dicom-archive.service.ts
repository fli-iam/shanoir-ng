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

