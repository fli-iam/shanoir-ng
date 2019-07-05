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

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'center-detail',
    templateUrl: 'center.component.html',
    styleUrls: ['center.component.css']
})

export class CenterComponent extends EntityComponent<Center> {

    private isNameUniqueError: boolean = false;
    private phoneNumberPatternError: boolean = false;
    private openAcqEq: boolean = true;

    constructor(
            private route: ActivatedRoute,
            private centerService: CenterService) {

        super(route, 'center');
    }

    get center(): Center { return this.entity; }
    set center(center: Center) { this.entity = center; }

    initView(): Promise<void> {
        return this.centerService.get(this.id).then(center => {
            this.center = center;
        });
    }

    initEdit(): Promise<void> {
        return this.centerService.get(this.id).then(center => {
            this.center = center;
        });
    }

    initCreate(): Promise<void> {
        this.entity = new Center();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.center.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]],
            'street': [this.center.street],
            'postalCode': [this.center.postalCode],
            'city': [this.center.city],
            'country': [this.center.country],
            'phoneNumber': [this.center.phoneNumber],
            'website': [this.center.website]
        });
    }

    private goToAcquisitionEquipment(acqE: AcquisitionEquipment) {
        this.router.navigate(['/acquisition-equipment/details/' + acqE.id]);
    }

    public hasEditRight(): boolean {
        return this.keycloakService.isUserAdminOrExpert();
    }

}