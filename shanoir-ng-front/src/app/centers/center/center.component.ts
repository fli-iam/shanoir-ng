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
import { UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { Selection } from 'src/app/studies/study/tree.service';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';
import {ShanoirValidators} from "../../shared/validators/shanoir-validators";
import { NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault, NgFor } from '@angular/common';
import { FormFooterComponent } from '../../shared/components/form-footer/form-footer.component';
import { HelpMessageComponent } from '../../shared/help-message/help-message.component';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';

@Component({
    selector: 'center-detail',
    templateUrl: 'center.component.html',
    styleUrls: ['center.component.css'],
    imports: [NgIf, FormsModule, ReactiveFormsModule, FormFooterComponent, NgSwitch, NgSwitchCase, HelpMessageComponent, NgSwitchDefault, NgFor, AcquisitionEquipmentPipe]
})

export class CenterComponent extends EntityComponent<Center> {

    isNameUniqueError: boolean = false;
    openAcqEq: boolean = true;

    get center(): Center { return this.entity; }
    set center(center: Center) { this.entity = center; }

    constructor(
            private route: ActivatedRoute,
            private centerService: CenterService) {

        super(route, 'center');
    }

    getService(): EntityService<Center> {
        return this.centerService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromCenter(this.center);
    }

    initView(): Promise<void> {
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.entity = new Center();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        return this.formBuilder.group({
            'name': [this.center.name, [ShanoirValidators.required, ShanoirValidators.minLength(2), ShanoirValidators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]],
            'street': [this.center.street],
            'postalCode': [this.center.postalCode, [Validators.pattern(/^[0-9]*$/)]],
            'city': [this.center.city],
            'country': [this.center.country],
            'phoneNumber': [this.center.phoneNumber, ShanoirValidators.isPhoneNumber()],
            'website': [this.center.website]
        });
    }

    goToAcquisitionEquipment(acqE: AcquisitionEquipment) {
        this.router.navigate(['/acquisition-equipment/details/' + acqE.id]);
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

}
