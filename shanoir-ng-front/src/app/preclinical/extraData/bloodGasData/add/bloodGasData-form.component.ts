import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';

import { BloodGasData }    from '../shared/bloodGasData.model';
import { BloodGasDataFile }    from '../shared/bloodGasDataFile.model';
import { ExaminationExtraDataService } from '../../extraData/shared/extradata.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

@Component({
  selector: 'bloodgas-data-upload-form',
  templateUrl: 'bloodGasData-form.component.html',
  providers: [ExaminationExtraDataService]
})
@ModesAware
export class BloodGasDataFormComponent implements OnInit {

  @Input() bloodGasData:BloodGasData = new BloodGasData();
  @Input() examination_id:number;
  @Input() isStandalone:boolean = false;
  @Output() closing = new EventEmitter();
  @Input() mode: Mode = new Mode();
  @Input() canModify: Boolean = false;
  newBloodGasdataForm: FormGroup;
  //urlupload:string;
  
   fileToUpload: File = null;
  @Output() bloodGasDataReady = new EventEmitter();
    
  constructor(
        private extradatasService: ExaminationExtraDataService,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private location: Location) {             
          
        }  
   
    
  ngOnInit(): void {
      this.buildForm();
  }
    
  buildForm(): void {
        this.newBloodGasdataForm = this.fb.group({
            
        });

        this.newBloodGasdataForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); 
    }

    onValueChanged(data?: any) {
        if (!this.newBloodGasdataForm) { return; }
        const form = this.newBloodGasdataForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        
    };
    
  getOut(bloodgas: BloodGasData = null): void {
      if (this.closing.observers.length > 0) {
          this.closing.emit(bloodgas);
          this.location.back();
      } else {
          this.location.back();
      }
  }
    
  createBloodGasData() {
        //if (!this.bloodGasData && !this.bloodGasData.fileUploadReady && !this.examination_id) { return; }
        if (!this.bloodGasData && !this.examination_id) { return; }
        this.extradatasService.create(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA,this.bloodGasData)
            .subscribe(bloodGasData => {
                //Then upload the file
               // bloodGasData.fileUploadReady = this.bloodGasData.fileUploadReady;
                //let uploadUrl:string = PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL+"/"+PreclinicalUtils.PRECLINICAL_EXTRA_DATA+PreclinicalUtils.PRECLINICAL_UPLOAD_URL+"/";
                //bloodGasData.fileUploadReady.launchRequest(uploadUrl.concat(bloodGasData.id)).subscribe();
                this.getOut(bloodGasData);
        });
  }
  
  
    fileChangeEvent(files: FileList){
    	this.fileToUpload = files.item(0);
    	this.bloodGasData.filename= this.fileToUpload.name;
    	let bloodGasDataFile: BloodGasDataFile = new BloodGasDataFile();
    	bloodGasDataFile.filename = this.fileToUpload.name;
    	bloodGasDataFile.bloodGasDataFile = this.fileToUpload;
    	if(!this.isStandalone){
    	 	this.bloodGasDataReady.emit(bloodGasDataFile);
    	 }
      	this.bloodGasData = new BloodGasData();
    }
    
}