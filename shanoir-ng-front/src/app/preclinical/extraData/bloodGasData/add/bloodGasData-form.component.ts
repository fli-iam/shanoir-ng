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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UntypedFormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { ExtraDataService } from '../../extraData/shared/extradata.service';
import { BloodGasDataFile } from '../shared/bloodGasDataFile.model';
import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { ExtraData } from '../../extraData/shared/extradata.model';


@Component({
    selector: 'bloodgas-data-upload-form',
    templateUrl: 'bloodGasData-form.component.html',
    animations: [slideDown],
    standalone: false
})
export class BloodGasDataFormComponent extends EntityComponent<BloodGasDataFile> {

    @Input() examinationId:number;
    @Input() canModify: boolean = false;

    @Output() bloodGasDataReady = new EventEmitter();

    constructor(
        private route: ActivatedRoute,
        private extradatasService: ExtraDataService) {

        super(route);
    }

    protected getRoutingName(): string {
        return 'preclinical-bloodgasdata';
    }

    get bloodGasData(): BloodGasDataFile { return this.entity; }
    set bloodGasData(bloodGasData: BloodGasDataFile) { this.entity = bloodGasData; }

    getService(): EntityService<any> {
        return this.extradatasService;
    }

    protected fetchEntity: () => Promise<BloodGasDataFile> = () => {
        return  this.extradatasService.getExtraDatas(this.examinationId).then(extradatas => {
            return this.loadExaminationExtraDatas(extradatas);
        });
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new BloodGasDataFile();
        return Promise.resolve();
    }

    loadExaminationExtraDatas(extradatas: ExtraData[]): BloodGasDataFile {
    	for (const ex of extradatas) {
    		// instanceof does not work??
    		if (ex.extraDataType == "Blood gas data") {
    			return ex as BloodGasDataFile;
    		}
    	}
        return new BloodGasDataFile();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
        });
    }

    public save(): Promise<BloodGasDataFile> {
        return this.extradatasService.createExtraData(PreclinicalUtils.PRECLINICAL_BLOODGAS_DATA,this.bloodGasData).then((bloodGasData) => {
            this.chooseRouteAfterSave(this.bloodGasData);
            this.consoleService.log('info', 'New preclinical bloodgasdata successfully saved with n° ' + bloodGasData.id);
            return bloodGasData;
        });
    }

    downloadFile() {
        this.extradatasService.downloadFile(this.entity.id);
    }

    fileChangeEvent(files: FileList){
    	this.bloodGasData.filename = files.item(0)?.name;
    	this.bloodGasData.bloodGasDataFile = files.item(0);
    	if(this.embedded){
    	 	this.bloodGasDataReady.emit(this.bloodGasData);
        }
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }
}
