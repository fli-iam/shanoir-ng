import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { Location } from '@angular/common';
import { ActivatedRoute, Router, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';

import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { ExaminationService } from '../shared/examination.service';
import { Examination } from '../shared/examination.model';
import { IdNameObject } from '../../shared/models/id-name-object.model';


@Component({
    selector: 'upload-extra-data',
    templateUrl: 'upload-extra-data.component.html'
})

export class UploadExtraDataComponent implements OnInit {

    public uploadExtraDataForm: FormGroup;
    public mode: "view" | "edit" | "create";
    fileToUpload: File = null;
    @Input() examinationStudyId: number;
    @Input() studies:  IdNameObject[];
    @Output() closing: EventEmitter<any> = new EventEmitter();
    public canModify: Boolean = false;
    private examination: Examination = new Examination();
    public examinationId: number;

    constructor(private route: ActivatedRoute, private router: Router,
        private fb: FormBuilder, private location: Location,
        private keycloakService: KeycloakService,  private examinationService: ExaminationService,) {

    }

    ngOnInit(): void {
        this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
        this.getExamination();
    }

    getExamination(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let examinationId = queryParams['id'];
                let mode = queryParams['mode'];
                if (examinationId) {
                    // view or edit mode
                    this.examinationId = examinationId;
                    return this.examinationService.getExamination(examinationId);
                } else {
                    // create mode
                    return Observable.of<Examination>();
                }
            })
            .subscribe((examination: Examination) => {
                this.examination = examination;
            });
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
          // do something, if upload success
          }, error => {
            console.log(error);
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