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
import { Selection } from 'src/app/studies/study/tree.service';

import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { ManufacturerModelPipe } from '../../acquisition-equipments/shared/manufacturer-model.pipe';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { CoilType } from '../shared/coil-type.enum';
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';

@Component({
    selector: 'coil',
    templateUrl: 'coil.component.html',
    styleUrls: ['coil.component.css'],
    standalone: false
})
export class CoilComponent extends EntityComponent<Coil> {

    centers: Center[] = [];
    manufModels: ManufacturerModel[] = [];
    coilTypes: CoilType[] = CoilType.all();

    constructor(
            private route: ActivatedRoute,
            private coilService: CoilService,
            private centerService: CenterService,
            public manufModelPipe: ManufacturerModelPipe) {
        super(route, 'coil');
    }

    get coil(): Coil { return this.entity; }
    set coil(coil: Coil) { this.entity = coil; }

    getService(): EntityService<Coil> {
        return this.coilService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromCoil(this.coil);
    }

    initView(): Promise<void> {
        return this.centerService.getAll().then(centers => {
            this.coil.center = centers.find(center => center.id == this.coil.center.id);
        });
    }

    initEdit(): Promise<void> {
        return this.centerService.getAll().then(centers => {
            this.centers = centers;
            this.coil.center = this.centers.filter(center => center.id == this.coil.center.id)[0];
            setTimeout(() => {
                this.updateManufList(this.coil.center);
            });
            this.coil.manufacturerModel = this.manufModels.filter(manuf => manuf.id == this.entity.manufacturerModel.id)[0];
        });
    }

    initCreate(): Promise<void> {
        this.entity = new Coil();
        const centerPromise: Promise<void> = this.centerService.getAll().then(centers => {
            this.centers = centers;
        });
        return centerPromise;
    }

    buildForm(): UntypedFormGroup {
        const form: UntypedFormGroup = this.formBuilder.group({
            'name': [this.coil.name, [Validators.required, Validators.minLength(2)]],
            'manufacturerModel': [{value: this.coil.manufacturerModel, disabled: !this.coil.center}, [Validators.required]],
            'center': [this.coil.center, [Validators.required]],
            'coilType': [this.coil.coilType],
            'numberOfChannels': [this.coil.numberOfChannels, [Validators.pattern(/^[0-9]*$/)]],
            'serialNumber': [this.coil.serialNumber],
        });
        this.subscriptions.push(
            form.valueChanges.subscribe((newValue: Coil) => {
                if (newValue.center) {
                    this.form.get('manufacturerModel').enable({onlySelf: true, emitEvent:false});
                }else {
                    this.form.get('manufacturerModel').disable({onlySelf: true, emitEvent:false});
                }
            }),
            form.get('center').valueChanges.subscribe((center: Center) => {
                this.updateManufList(center);
            })
        );
        return form;
    }

    updateManufList(center: Center): void {
        if (!this.form.get('manufacturerModel').disabled) {
            if (this.form) this.form.get('manufacturerModel').markAsUntouched();
            this.manufModels = center?.acquisitionEquipments?.map(acqEq => acqEq.manufacturerModel);
            if (!this.coil.manufacturerModel || !this.manufModels?.find(model => model.id == this.coil.manufacturerModel.id)) {
                this.coil.manufacturerModel = this.manufModels?.[0];
            }
        }
    }

    public async hasEditRight(): Promise<boolean> {
        return this.keycloakService.isUserAdminOrExpert();
    }

    openNewCenter() {
        this.navigateToAttributeCreateStep('/center/create', 'center');
    }

    openNewManufModel() {
        this.navigateToAttributeCreateStep('/manufacturer-model/create', 'manufacturerModel');
    }

}
