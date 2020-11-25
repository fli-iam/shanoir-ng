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
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { ModesAware } from '../../../shared/mode/mode.decorator';
import { Pathology } from '../shared/pathology.model';
import { PathologyService } from '../shared/pathology.service';


@Component({
    selector: 'pathology-form',
    templateUrl: 'pathology-form.component.html',
    providers: [PathologyService]
})
@ModesAware
export class PathologyFormComponent extends EntityComponent<Pathology>{

    constructor(
        private route: ActivatedRoute,
        private pathologyService: PathologyService) {

            super(route, 'preclinical-pathology');
    }

    get pathology(): Pathology { return this.entity; }
    set pathology(pathology: Pathology) { this.entity = pathology; }

    initView(): Promise<void> {
        return this.pathologyService.get(this.id).then(pathology => {
            this.pathology = pathology;
        });
    }

    initEdit(): Promise<void> {
        return this.pathologyService.get(this.id).then(pathology => {
            this.pathology = pathology;
        });
    }

    initCreate(): Promise<void> {
        this.entity = new Pathology();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.pathology.name, Validators.required]
        });
    }

    
}