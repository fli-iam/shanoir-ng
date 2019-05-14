import { Location } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';

@Component({
    selector: 'upload-extra-data',
    templateUrl: 'upload-extra-data.component.html'
})

export class UploadExtraDataComponent implements OnInit {

    public uploadExtraDataForm: FormGroup;
    public mode: "view" | "edit" | "create";
    fileToUpload: File = null;
    @Input() examination: Examination;
    @Input() studies:  IdName[];
    @Output() closing: EventEmitter<any> = new EventEmitter();
    public canModify: Boolean = false;

    constructor(
            private fb: FormBuilder, 
            private location: Location,
            private keycloakService: KeycloakService,
            private examinationService: ExaminationService,
            private msgService: MsgBoxService) {

    }

    ngOnInit(): void {
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    buildForm(): void {
        this.uploadExtraDataForm = this.fb.group({
            add: 'add',
            studyId: 'studyId',
            examination: 'examination'
        });

    }

    handleFileInput(files: FileList) {
        this.fileToUpload = files.item(0);
    }

    uploadFileToActivity() {
        this.examinationService.postFile(this.fileToUpload).subscribe(data => {
            this.msgService.log('info', 'The file has been sucessfully uploaded');
        });
      }

    formErrors = {
        'add': ''
    };

   back(id?: number): void {
        if (this.closing.observers.length > 0) {
           this.closing.emit(id);
        } else {
            this.location.back();
        }
    }

    add(): void {
      
    }


}