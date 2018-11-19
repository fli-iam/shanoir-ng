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


    protected save(): Promise<void> {
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
    
}