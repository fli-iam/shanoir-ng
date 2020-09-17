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

import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Manufacturer } from '../shared/manufacturer.model';
import { ManufacturerService } from '../shared/manufacturer.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

@Component({
    selector: 'manufacturer-detail',
    templateUrl: 'manufacturer.component.html'
})

export class ManufacturerComponent extends EntityComponent<Manufacturer> {
    
    isNameUniqueError = null;

    constructor (
            private route: ActivatedRoute,
            private manufService: ManufacturerService) {

        super(route, 'manufacturer');
    }

    get manuf(): Manufacturer { return this.entity; }
    set manuf(manuf: Manufacturer) { this.entity = manuf; }

    getService(): EntityService<Manufacturer> {
        return this.manufService;
    }

    initView(): Promise<void> {
        return this.getManufacturer();
    }

    initEdit(): Promise<void> {
        return this.getManufacturer();
    }

    initCreate(): Promise<void> {
        this.entity = new Manufacturer();
        return Promise.resolve();
    }

    getManufacturer(): Promise<void> {
        return this.manufService.get(this.id)
            .then(manuf => {
                this.manuf = manuf;
            });
    }   

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.manuf.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]]
        });

    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }
}