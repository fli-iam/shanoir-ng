import { Component, OnInit, Input, Output, EventEmitter, ViewChild, ElementRef } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Location } from '@angular/common';


import { PhysiologicalData }    from '../shared/physiologicalData.model';
import { PhysiologicalDataFile }    from '../shared/physiologicalDataFile.model';
import { ExaminationExtraDataService } from '../../extraData/shared/extradata.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

@Component({
  selector: 'physiological-data-upload-form',
  templateUrl: 'physiologicalData-form.component.html',
  providers: [ExaminationExtraDataService]
})
@ModesAware
export class PhysiologicalDataFormComponent implements OnInit {

  @Input() physioData:PhysiologicalData = new PhysiologicalData();
  @Input() examination_id:number;
  @Input() isStandalone:boolean = false;
  @Output() closing = new EventEmitter();
  @Input() mode: Mode = new Mode();
  @Input() canModify: Boolean = false;
  newPhysiodataForm: FormGroup;
  urlupload:string;
  @Output() physioDataReady = new EventEmitter();
  fileToUpload: File = null;
  
  
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
        this.newPhysiodataForm = this.fb.group({
            'has_heart_rate':[this.physioData.has_heart_rate],
            'has_respiratory_rate':[this.physioData.has_respiratory_rate],
            'has_sao2':[this.physioData.has_sao2],
            'has_temperature':[this.physioData.has_temperature],
        });

        this.newPhysiodataForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged(); 
    }

    onValueChanged(data?: any) {
        if (!this.newPhysiodataForm) { return; }
        const form = this.newPhysiodataForm;
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
        'has_heart_rate':'',
        'has_respiratory_rate':'',
        'has_sao2':'',
        'has_temperature':''
  };
    
  getOut(physiodata: PhysiologicalData = null): void {
      if (this.closing.observers.length > 0) {
          this.closing.emit(physiodata);
          this.location.back();
      } else {
          this.location.back();
      }
  }
    
  createPhysioData() {
        //if (!this.physioData && !this.physioData.fileUploadReady && !this.examination_id) { return; }
        if (!this.physioData  && !this.examination_id) { return; }
      
        this.physioData.examination_id = this.examination_id;
        this.extradatasService.create(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA,this.physioData)
            .subscribe(physioData => {
                //Then upload the file
                //physioData.fileUploadReady = this.physioData.fileUploadReady;
                //let uploadUrl:string = PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL+"/"+PreclinicalUtils.PRECLINICAL_EXTRA_DATA+PreclinicalUtils.PRECLINICAL_UPLOAD_URL+"/";
                //physioData.fileUploadReady.launchRequest(uploadUrl.concat(physioData.id)).subscribe();
                this.getOut(physioData);
        });
  }
  
  fileChangeEvent(files: FileList){
    	this.fileToUpload = files.item(0);
    	this.physioData.filename= this.fileToUpload.name;
    	let physioDataFile: PhysiologicalDataFile = new PhysiologicalDataFile();
    	physioDataFile.filename = this.fileToUpload.name;
    	physioDataFile.physiologicalDataFile = this.fileToUpload;
    	physioDataFile.has_heart_rate = this.physioData.has_heart_rate;
    	physioDataFile.has_respiratory_rate = this.physioData.has_respiratory_rate;
    	physioDataFile.has_sao2 = this.physioData.has_sao2;
    	physioDataFile.has_temperature = this.physioData.has_temperature;
    	this.emitEvent(physioDataFile);
      	this.physioData = new PhysiologicalData();
    }
    
  isYesOrNo(value:boolean): string{
      if(value) return 'Yes';
      return 'No';
  }
  
  emitEvent(physioDataFile : PhysiologicalDataFile) {
  	if(!this.isStandalone){
    	this.physioDataReady.emit(physioDataFile);
    }
  }
  
  changePhysio(){
  	let physioDataFile: PhysiologicalDataFile = new PhysiologicalDataFile();
    physioDataFile.filename = this.physioData.filename;
    physioDataFile.has_heart_rate = this.physioData.has_heart_rate;
    physioDataFile.has_respiratory_rate = this.physioData.has_respiratory_rate;
    physioDataFile.has_sao2 = this.physioData.has_sao2;
    physioDataFile.has_temperature = this.physioData.has_temperature;
  	this.emitEvent(physioDataFile);
  }
    
}