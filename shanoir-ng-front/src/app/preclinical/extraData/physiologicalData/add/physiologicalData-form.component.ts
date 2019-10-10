import { Component, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';


import { PhysiologicalData }    from '../shared/physiologicalData.model';
import { PhysiologicalDataFile }    from '../shared/physiologicalDataFile.model';
import { ExtraDataService } from '../../extraData/shared/extradata.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Mode } from "../../../shared/mode/mode.model";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { ExtraData } from '../../extraData/shared/extradata.model';

@Component({
  selector: 'physiological-data-upload-form',
  templateUrl: 'physiologicalData-form.component.html',
  providers: [ExtraDataService],
  animations: [slideDown]
})
@ModesAware
export class PhysiologicalDataFormComponent extends EntityComponent<PhysiologicalData> {

    @Input() examination_id:number;
    @Input() isStandalone:boolean = false;
    @Input() canModify: Boolean = false;
    @Output() physioDataReady = new EventEmitter();
  
    urlupload:string;
    fileToUpload: File = null;
    @Output() bloodGasDataReady = new EventEmitter();

    constructor(
        private route: ActivatedRoute,
        private extradatasService: ExtraDataService) {

        super(route, 'preclinical-physiogicaldata');
    }

    get physioData(): PhysiologicalData { return this.entity; }
    set physioData(physioData: PhysiologicalData) { this.entity = physioData; }

    initView(): Promise<void> {
        this.entity = new PhysiologicalData();
        this.extradatasService.getExtraDatas(this.examination_id).then(extradatas => {
            this.loadExaminationExtraDatas(extradatas);
        });
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.entity = new PhysiologicalData();
        this.extradatasService.getExtraDatas(this.examination_id).then(extradatas => {
            this.loadExaminationExtraDatas(extradatas);
        });
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new PhysiologicalData();
        return Promise.resolve();
    }

    loadExaminationExtraDatas(extradatas: ExtraData[]){
    	for (let ex of extradatas) {
    		// instanceof does not work??
    		if (ex.extradatatype == "Physiological data"){
    			this.physioData = <PhysiologicalData>ex;
    		}
    	}
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'has_heart_rate':[this.physioData.has_heart_rate],
            'has_respiratory_rate':[this.physioData.has_respiratory_rate],
            'has_sao2':[this.physioData.has_sao2],
            'has_temperature':[this.physioData.has_temperature],
        });
    }

    protected save(): Promise<void> {
        this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA,this.physioData).subscribe((physioData) => {
            this.chooseRouteAfterSave(this.physioData);
            this.msgBoxService.log('info', 'The new preclinical-physiogicaldata has been successfully saved under the number ' + physioData.id);
        });
        return Promise.resolve();
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