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

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';


import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { PhysiologicalDataFile }    from '../shared/physiologicalDataFile.model';
import { ExtraDataService } from '../../extraData/shared/extradata.service';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { ExtraData } from '../../extraData/shared/extradata.model';

@Component({
    selector: 'physiological-data-upload-form',
    templateUrl: 'physiologicalData-form.component.html',
    animations: [slideDown],
    standalone: false
})
export class PhysiologicalDataFormComponent extends EntityComponent<PhysiologicalDataFile> {

    @Input() examinationId:number;
    @Input() canModify: boolean = false;
    @Output() physioDataReady: EventEmitter<PhysiologicalDataFile> = new EventEmitter();

    urlupload:string;
    fileToUpload: File = null;

    constructor(
        private route: ActivatedRoute,
        private extradatasService: ExtraDataService) {

        super(route, 'preclinical-physiogicaldata');
    }

    get physioData(): PhysiologicalDataFile { return this.entity; }
    set physioData(physioData: PhysiologicalDataFile) { this.entity = physioData; }

    // Note: should be getService(): EntityService<PhysiologicalData> {
    getService(): EntityService<any> {
        return this.extradatasService;
    }

    protected fetchEntity: () => Promise<PhysiologicalDataFile> = () => {
        return  this.extradatasService.getExtraDatas(this.examinationId).then(extradatas => {
            const physioData: PhysiologicalDataFile = this.getExaminationExtraDatas(extradatas);
            this.emitEvent(physioData);
            return physioData;
        });
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new PhysiologicalDataFile();
        return Promise.resolve();
    }

    getExaminationExtraDatas(extradatas: ExtraData[]): PhysiologicalDataFile {
    	for (const ex of extradatas) {
    		// instanceof does not work??
    		if (ex.extradatatype == "Physiological data"){
    			return ex as PhysiologicalDataFile;
    		}
    	}
        return new PhysiologicalDataFile();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'hasHeartRate':[this.physioData.hasHeartRate],
            'hasRespiratoryRate':[this.physioData.hasRespiratoryRate],
            'hasSao2':[this.physioData.hasSao2],
            'hasTemperature':[this.physioData.hasTemperature],
        });
    }

    public save(): Promise<PhysiologicalDataFile> {
        return this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_PHYSIO_DATA,this.physioData).then((physioData) => {
            this.chooseRouteAfterSave(this.physioData);
            this.consoleService.log('info', 'New preclinical physiogicaldata successfully saved with n° ' + physioData.id);
            return physioData;
        });
    }

    fileChangeEvent(files: FileList){
    	this.fileToUpload = files.item(0);
    	this.physioData.filename = this.fileToUpload.name;
    	this.emitEvent(this.physioData);
    }

    isYesOrNo(value:boolean): string{
        if(value) return 'Yes';
        return 'No';
    }

    emitEvent(physioData: PhysiologicalDataFile = this.physioData) {
        this.physioDataReady.emit(physioData);
    }

    downloadFile() {
        this.extradatasService.downloadFile(this.entity.id);
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }


}
