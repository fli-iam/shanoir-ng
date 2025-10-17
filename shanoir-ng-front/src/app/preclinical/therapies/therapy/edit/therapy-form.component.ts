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
import { UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { slideDown } from '../../../../shared/animations/animations';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { TherapyType } from '../../../shared/enum/therapyType';
import { Therapy } from '../shared/therapy.model';
import { TherapyService } from '../shared/therapy.service';



@Component({
    selector: 'therapy-form',
    templateUrl: 'therapy-form.component.html',
    animations: [slideDown],
    standalone: false
})
export class TherapyFormComponent extends EntityComponent<Therapy>{

    TherapyType = TherapyType;
    public isTherapyUnique: boolean = true;

    constructor(
        private route: ActivatedRoute,
        private therapyService: TherapyService,
        private referenceService: ReferenceService) {

            super(route, 'preclinical-therapy');
        }

    get therapy(): Therapy { return this.entity; }
    set therapy(therapy: Therapy) { this.entity = therapy; }

    getService(): EntityService<Therapy> {
        return this.therapyService;
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new Therapy();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'name': [this.therapy.name, [Validators.required, this.registerOnSubmitValidator('unique', 'name')]],
            'therapyType': [this.therapy.therapyType, Validators.required],
            'comment': [this.therapy.comment]
        });
    }

}
