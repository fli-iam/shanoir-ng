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

import { Component,  Input, Output, EventEmitter } from '@angular/core';
import { FormGroup} from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { BloodGasData }    from '../shared/bloodGasData.model';
import { BloodGasDataFile }    from '../shared/bloodGasDataFile.model';
import { ExtraDataService } from '../../extraData/shared/extradata.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { ExtraData } from '../../extraData/shared/extradata.model';
import { MsgBoxService } from '../../../../shared/msg-box/msg-box.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';


@Component({
  selector: 'bloodgas-data-upload-form',
  templateUrl: 'bloodGasData-form.component.html',
  providers: [ExtraDataService],
  animations: [slideDown]
})
@ModesAware
export class BloodGasDataFormComponent extends EntityComponent<BloodGasData> {

    @Input() examination_id:number;
    @Input() isStandalone:boolean = false;
    @Input() canModify: Boolean = false;
  
    fileToUpload: File = null;
    @Output() bloodGasDataReady = new EventEmitter();

    constructor(
        private route: ActivatedRoute,
        private extradatasService: ExtraDataService) {

        super(route, 'preclinical-bloodgasdata');
    }

    get bloodGasData(): BloodGasData { return this.entity; }
    set bloodGasData(bloodGasData: BloodGasData) { this.entity = bloodGasData; }
    
    getService(): EntityService<BloodGasData> {
        return this.extradatasService;
    }
   
    initView(): Promise<void> {
        this.entity = new BloodGasData();
        this.extradatasService.getExtraDatas(this.examination_id).then(extradatas => {
            this.loadExaminationExtraDatas(extradatas);
        });
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.entity = new BloodGasData();
        this.extradatasService.getExtraDatas(this.examination_id).then(extradatas => {
            this.loadExaminationExtraDatas(extradatas);
        });
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new BloodGasData();
        return Promise.resolve();
    }

    loadExaminationExtraDatas(extradatas: ExtraData[]){
    	for (let ex of extradatas) {
    		// instanceof does not work??
    		if (ex.extradatatype != "Physiological data"){
    			this.bloodGasData = <BloodGasData>ex;
    		}
    	}
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
        });
    }


    public save(): Promise<void> {
        this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA,this.bloodGasData).subscribe((bloodGasData) => {
            this.chooseRouteAfterSave(this.bloodGasData);
            this.msgBoxService.log('info', 'The new preclinical-bloodgasdata has been successfully saved under the number ' + bloodGasData.id);
        });
        return Promise.resolve();
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

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }

    
}