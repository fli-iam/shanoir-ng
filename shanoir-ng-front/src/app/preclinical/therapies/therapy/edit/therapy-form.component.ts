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

import { Component } from '@angular/core';
import { FormGroup,  Validators } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';

import { Therapy }    from '../shared/therapy.model';
import { TherapyService } from '../shared/therapy.service';
import { ReferenceService } from '../../../reference/shared/reference.service';

import { Enum } from "../../../../shared/utils/enum";
import { EnumUtils } from "../../../shared/enum/enumUtils";
import { slideDown } from '../../../../shared/animations/animations';
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';


@Component({
    selector: 'therapy-form',
    templateUrl: 'therapy-form.component.html',
    providers: [TherapyService, ReferenceService],
    animations: [slideDown]
})
@ModesAware
export class TherapyFormComponent extends EntityComponent<Therapy>{

    therapyTypes: Enum[] = [];
    private isTherapyUnique: Boolean = true;
    
    constructor(
        private route: ActivatedRoute,
        private therapyService: TherapyService, 
        private referenceService: ReferenceService,
        private enumUtils: EnumUtils) {

            super(route, 'preclinical-therapy');
        }
   
    get therapy(): Therapy { return this.entity; }
    set therapy(therapy: Therapy) { this.entity = therapy; }

    getService(): EntityService<Therapy> {
        return this.therapyService;
    }

    initView(): Promise<void> {
        this.getEnums();
        return this.therapyService.get(this.id).then(therapy => {
            this.therapy = therapy;
        });
    }

    initEdit(): Promise<void> {
        this.getEnums();
        return this.therapyService.get(this.id).then(therapy => {
            this.therapy = therapy;
        });
    }

    initCreate(): Promise<void> {
        this.getEnums();
        this.entity = new Therapy();
        return Promise.resolve();
    }

    getEnums(): void {
        this.therapyTypes = this.enumUtils.getEnumArrayFor('TherapyType');
    }


    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.therapy.name, Validators.required],
            'therapyType': [this.therapy.therapyType, Validators.required],
            'comment': [this.therapy.comment]
        });
    }

}