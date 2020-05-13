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
import { FormGroup, FormBuilder } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';

import { ExtraData }    from '../shared/extradata.model';
import { ExtraDataService } from '../shared/extradata.service';

import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Mode } from "../../../shared/mode/mode.model";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { slideDown } from '../../../../shared/animations/animations';

@Component({
    selector: 'extra-data-upload-form',
    templateUrl: 'extradata-form.component.html',
    providers: [ExtraDataService],
    animations: [slideDown]
})
@ModesAware
export class ExtraDataFormComponent extends EntityComponent<ExtraData>{

    @Input() examination_id: number;
    @Input() isStandalone: boolean = false;
    @Input() canModify: Boolean = false;
    @Output() extradataReady = new EventEmitter();

    constructor(
        private route: ActivatedRoute,
        private extradataService: ExtraDataService) {

        super(route, 'preclinical-extradata');
    }

    get extradata(): ExtraData { return this.entity; }
    set extradata(extradata: ExtraData) { this.entity = extradata; }

    initView(): Promise<void> {
        return this.extradataService.getExtraData(""+this.examination_id).then(extradata => {
            this.extradata = extradata;
        });
    }

    initEdit(): Promise<void> {
        return this.extradataService.getExtraData(""+this.examination_id).then(extradata => {
            this.extradata = extradata;
        });
    }

    initCreate(): Promise<void> {
        this.entity = new ExtraData();
        return Promise.resolve();
    }


    buildForm(): FormGroup {
        return this.formBuilder.group({
        });
    }


    


    createExtraData() {
        //if (!this.extradata && !this.extradata.fileUploadReady && !this.examination_id) { return; }
        if (!this.extradata && !this.examination_id) { return; }
        this.extradata.examination_id = this.examination_id;
        this.extradataService.createExtraData(PreclinicalUtils.PRECLINICAL_EXTRA_DATA, this.extradata)
            .subscribe(extradata => {
                //Then upload the file
                //extradata.fileUploadReady = this.extradata.fileUploadReady;
                //let uploadUrl: string = PreclinicalUtils.PRECLINICAL_API_EXAMINATION_URL + "/" + PreclinicalUtils.PRECLINICAL_EXTRA_DATA + PreclinicalUtils.PRECLINICAL_UPLOAD_URL + "/";
                //extradata.fileUploadReady.launchRequest(uploadUrl.concat(extradata.id)).subscribe();
            });
    }

    onFileSelected(event) {
        //this.extradata.fileUploadReady = event;
        this.extradata.filename = event.filename;
        if (!this.isStandalone) this.extradataReady.emit(this.extradata);
        this.extradata = new ExtraData();
    }
    
    downloadExtraData = (extradata:ExtraData) => {
        window.open(this.extradataService.getDownloadUrl(extradata));
    }

}
