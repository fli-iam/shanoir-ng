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

import { Component, Input } from '@angular/core';
import { UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { Selection } from 'src/app/studies/study/tree.service';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { ManufacturerModelPipe } from '../../acquisition-equipments/shared/manufacturer-model.pipe';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
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

    @Input() acqEquip: AcquisitionEquipment;
    centers: Center[] = [];
    manufModels: ManufacturerModel[] = [];
    coilTypes: CoilType[] = CoilType.all();
    prefilledCenter: Center;
    prefilledManuf: ManufacturerModel;

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
            if (this.acqEquip) {
                this.coil.center = this.acqEquip.center;
                this.coil.manufacturerModel = this.acqEquip.manufacturerModel;
            } else {
                this.coil.center = this.centers.filter(center => center.id == this.coil.center.id)[0];
                this.updateManufList(this.coil.center?.id);
                this.coil.manufacturerModel = this.manufModels.filter(manuf => manuf.id == this.entity.manufacturerModel.id)[0];
            }
        });
    }

    initCreate(): Promise<void> {
        this.entity = new Coil();
        this.breadcrumbsService.currentStep.getPrefilledValue('center').then(res => this.prefilledCenter = res);
        let centerPromise: Promise<void>;
        if (this.prefilledCenter) {
            this.coil.center = this.prefilledCenter;
            this.centers = [this.prefilledCenter];
            centerPromise = Promise.resolve();
        } else {
            centerPromise = this.centerService.getAll().then(centers => {
                this.centers = centers;
            });
        }
        this.breadcrumbsService.currentStep.getPrefilledValue('manufacturerModel').then(res => this.prefilledManuf = res);
        if (this.prefilledManuf) {
            this.coil.manufacturerModel = this.prefilledManuf;
            this.manufModels = [this.prefilledManuf];
        }
        return centerPromise;
    }

    buildForm(): UntypedFormGroup {
        let form: UntypedFormGroup = this.formBuilder.group({
            'name': [this.coil.name, [Validators.required, Validators.minLength(2)]],
            'acquiEquipModel': [{value: this.coil.manufacturerModel, disabled: this.prefilledManuf}, [Validators.required]],
            'center': [{value: this.coil.center, disabled: this.prefilledCenter}, [Validators.required]],
            'coilType': [this.coil.coilType],
            'nbChannel': [this.coil.numberOfChannels],
            'serialNb': [this.coil.serialNumber]
        });
        form.valueChanges.subscribe((newValue: Coil) => {
            if (newValue.center && !this.prefilledManuf) {
                this.form.get('acquiEquipModel').enable({onlySelf: true, emitEvent:false});
            }
            else {
                this.form.get('acquiEquipModel').disable({onlySelf: true, emitEvent:false});
            }
        });
        form.get('center').valueChanges.subscribe((centerId: number) => {
            this.updateManufList(centerId);
        });
        return form;
    }

    protected mapFormToEntity() {
        this.coil.name = this.form.get('name').value;
        this.coil.center = this.form.get('center').value;
        this.coil.manufacturerModel = this.form.get('acquiEquipModel').value;
        this.coil.coilType = this.form.get('coilType').value;
        this.coil.numberOfChannels = this.form.get('nbChannel').value;
        this.coil.serialNumber = this.form.get('serialNb').value;
    }

    updateManufList(centerId: number): void {
        if (!this.prefilledManuf) {
            const center: Center = this.centers?.find(c => c.id == centerId);
            this.coil.center = center;
            if (this.form) this.form.get('acquiEquipModel').markAsUntouched();
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
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/center/create']).then(success => {
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                    this.entity.center = entity as Center;
                })
            );
        });
    }

    openNewManufModel() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(success => {
            this.breadcrumbsService.currentStep.addPrefilled('center', this.coil.center);
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                    this.entity.manufacturerModel = (entity as AcquisitionEquipment).manufacturerModel;
                    this.entity.center = this.centers.find(c => c.id == (entity as AcquisitionEquipment).center?.id);
                })
            );
        });
    }

}
