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

import { Component, ElementRef, Input, Output, EventEmitter, ViewChild, Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { HttpClient } from '@angular/common/http';

import {FileUploadReady} from './fileUploadReady.model';

@Component({
    selector: 'file-upload',
    template: '<input type="file" [multiple]="multiple" #fileInput>'
})

export class FileUploadComponent {
    @Input() url:string;
    @Input() extraFields:string[];
    @Input() multiple: boolean = false;
    @Input() auto: boolean = false;
    @ViewChild('fileInput') inputEl: ElementRef;
    @Output() selectedFile = new EventEmitter();
    
    fileUploadReady:FileUploadReady;
    progress$: any;
    progress: any;
    progressObserver: any;

    constructor(private http: HttpClient) {
       this.progress$ = Observable.create(observer => {
        this.progressObserver = observer}).share();    
    }
    
    private prepareUploadRequest(){
        
        this.fileUploadReady = new FileUploadReady();
        let inputEl: HTMLInputElement = this.inputEl.nativeElement;
        let fileCount: number = inputEl.files.length;            
        let formData = new FormData();
        this.fileUploadReady.xhr = new XMLHttpRequest();
        this.fileUploadReady.formData = new FormData();
    
        if (fileCount > 0) { // a file was selected
            for (let i = 0; i < fileCount; i++) {
                formData.append('files', inputEl.files.item(i), inputEl.files.item(i).name);
                this.fileUploadReady.filename = inputEl.files.item(i).name;
            }
            
            /*
            if(this.extraFields){
                let extraFieldCount = this.extraFields.length;
                for (let i = 0; i < extraFieldCount; i++) {
                    formData.append('files', inputEl.files.item(i), inputEl.files.item(i).name);
                }
            }
            */
            
        }
        this.fileUploadReady.formData = formData;
        if(this.url && this.auto == true){
            this.uploadRequest(this.url).subscribe();
        }else{
            this.selectedFile.emit(this.fileUploadReady);
        }
        
    }
    
    
    private uploadRequest (url: string): Observable<any> {
        return Observable.create(observer => {
            this.fileUploadReady.xhr.onreadystatechange = () => {
                if (this.fileUploadReady.xhr.readyState === 4) {
                    if (this.fileUploadReady.xhr.status === 200) {
                        observer.next(JSON.parse(this.fileUploadReady.xhr.response));
                        observer.complete();
                    } else {
                        observer.error(this.fileUploadReady.xhr.response);
                    }
                }
            };
                       
            this.fileUploadReady.xhr.upload.onprogress = (event) => {
               this.progress = Math.round(event.loaded / event.total * 100);   
               //this.progressObserver.next(this.progress);
            };
            
            this.fileUploadReady.xhr.open('POST', url, true);
            this.fileUploadReady.xhr.send(this.fileUploadReady.formData);
               
         });
    }
    /*
    upload() {
        if(this.url){
            this.uploadRequest(this.url).subscribe(item => {
                        this.selectedFile.emit(item);
                    });
        }else{
            console.error('No url to upload to!');
        }        
    }
    */
}