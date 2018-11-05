import { Component, ViewChild, ElementRef, Output, EventEmitter, Input } from '@angular/core';
import { ImagesUrlUtil } from '../../utils/images-url.util';


@Component({
    selector: 'upload-file',
    templateUrl: 'uploader.component.html',
    styleUrls: ['uploader.component.css']
})
export class UploaderComponent {

    @ViewChild('input') private fileInput: ElementRef;
    @Output() fileChange = new EventEmitter<any>();
    @Input() loading: boolean = false;
    @Input() error: boolean = false;
    private readonly ImagesUrlUtil = ImagesUrlUtil;
    private filename: string;
    
    private click() {
        this.fileInput.nativeElement.click();
    }

    private changeFile(file: any) {
        this.filename = undefined;
        if (file && file.target && file.target.files && file.target.files[0]) this.filename = file.target.files[0].name;
        this.fileChange.emit(file);
    }

}