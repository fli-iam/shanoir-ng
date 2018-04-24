import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Rx';

declare var JSZip: any;

@Injectable()
export class DicomArchiveService {

 private fileReader: FileReader;

 constructor() {}

 importFromZip(ev): Observable<any> {

	 if (this.fileReader == null){
		 this.fileReader = new FileReader();
	 }
	 
	 this.fileReader.readAsArrayBuffer((<any>ev.target).files[0]);
	
	 return Observable.create(observer => {
	 // if success
		 this.fileReader.onload = ev => {
			 observer.next(this.fileReader);
		 }
		 // if failed
		 this.fileReader.onerror = error => observer.error(error);
	 });
 }
	
 clearFileInMemory() {
	 this.fileReader = undefined;
 }

 extractFileDirectoryStructure(): Observable<any>{
	 return Observable.create(observer => {
		 var zip = new JSZip();
		 zip.loadAsync(this.fileReader.result).then(function (x) {
			 observer.next(x);
		 });
	 });
 }
}

