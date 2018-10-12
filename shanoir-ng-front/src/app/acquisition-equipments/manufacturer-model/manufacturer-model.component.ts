import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { Enum } from '../../shared/utils/enum';
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';
import { Manufacturer } from '../shared/manufacturer.model';
import { ManufacturerService } from '../shared/manufacturer.service';

@Component({
    selector: 'manufacturer-model-detail',
    templateUrl: 'manufacturer-model.component.html'
})

export class ManufacturerModelComponent extends EntityComponent<ManufacturerModel> {

    private datasetModalityTypes: Enum[] = [];
    private manufs: Manufacturer[];

    constructor(
            private route: ActivatedRoute,
            private manufModelService: ManufacturerModelService, 
            private manufService: ManufacturerService) {

        super(route, 'manufacturer-model');
    }

    private get manufModel(): ManufacturerModel { return this.entity; }
    private set manufModel(manufModel: ManufacturerModel) { this.entity = manufModel; }


    initView(): Promise<void> {
        return this.getManufacturerModel();
    }

    initEdit(): Promise<void> {
        let manufModelPromise: Promise<void> = this.getManufacturerModel();
        Promise.all([
            manufModelPromise,
            this.getManufs()
        ]).then(([, ]) => {
            this.manufModel.manufacturer = this.getManufById(this.manufModel.manufacturer.id);
        });
        return manufModelPromise;
    }

    initCreate(): Promise<void> {
        this.getManufs();
        this.entity = new ManufacturerModel();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        let magneticFieldFC: FormControl;
        if (this.isMR) {
            magneticFieldFC = new FormControl(this.manufModel.magneticField, Validators.required);
        } else {
            magneticFieldFC = new FormControl(this.manufModel.magneticField);
        }
        return this.formBuilder.group({
            'name': [this.manufModel.name, [Validators.required, Validators.minLength(2), Validators.maxLength(200), this.registerOnSubmitValidator('unique', 'name')]],
            'manufacturer': [this.manufModel.manufacturer, Validators.required],
            'magneticField': magneticFieldFC,
            'datasetModalityType': [this.manufModel.datasetModalityType, Validators.required]
        });
    }

    private get isMR(): boolean { 
        return this.manufModel && this.manufModel.datasetModalityType == 'MR_DATASET'; 
    }

    private getManufacturerModel(): Promise<void> {
        return this.manufModelService.get(this.id)
            .then(manufModel => {
                this.manufModel = manufModel;
            });
    }

    private getManufs(): Promise<void> {
        return this.manufService.getAll()
            .then(manufs => {
                this.manufs = manufs;
            });
    }

    private getManufById(id: number): Manufacturer {
        for (let manuf of this.manufs) {
            if (id == manuf.id) {
                return manuf;
            }
        }
        return null;
    }

    private openNewManuf() {
        let currentStep: Step = this.breadcrumbsService.lastStep;
        this.router.navigate(['/manufacturer/create']).then(success => {
            currentStep.waitFor(this.breadcrumbsService.lastStep).subscribe(entity => {
                (currentStep.entity as ManufacturerModel).manufacturer = entity as Manufacturer;
            });
        });
    }
}