import { Component } from '@angular/core';
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatasetModalityType } from '../../shared/enums/dataset-modality-type';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { ShanoirError } from '../../shared/models/error.model';

@Component({
    selector: 'acquisition-equipment-detail',
    templateUrl: 'acquisition-equipment.component.html'
})

export class AcquisitionEquipmentComponent extends EntityComponent<AcquisitionEquipment> {

    private uniqueSerialError: boolean = false;
    private manufModels: ManufacturerModel[];
    private centers: Center[];
    private datasetModalityTypeEnumValue: string;
    private nonEditableCenter: boolean = false;

    private get acqEquip(): AcquisitionEquipment { return this.entity; }
    private set acqEquip(acqEquip: AcquisitionEquipment) { this.entity = acqEquip; }
    
    constructor(
            private route: ActivatedRoute, 
            private acqEquipService: AcquisitionEquipmentService, 
            private manufModelService: ManufacturerModelService,
            private centerService: CenterService) {

        super(route, 'acquisition-equipment');
        this.manageSaveErrors();
    }

    initView(): Promise<void> {
        return this.acqEquipService.get(this.id).then(ae => {
            this.acqEquip = ae;
            this.updateAcquEq();
        });
    }

    initEdit(): Promise<void> {
        this.getManufModels();
        return Promise.all([
            this.centerService.getCentersNames(),
            this.acqEquipService.get(this.id)
        ]).then(([centers, ae]) => {
            this.centers = centers;
            this.acqEquip = ae;
            this.updateAcquEq();
        });
    }

    initCreate(): Promise<void> {
        this.entity = new AcquisitionEquipment();
        this.centerService.getCentersNames().then(centers => this.centers = centers);
        this.getManufModels();
        return Promise.resolve();
    }

    private prefill() {
        this.nonEditableCenter = this.breadcrumbsService.lastStep.isPrefilled('center');
        if (this.nonEditableCenter) {
            this.acqEquip.center = this.breadcrumbsService.lastStep.getPrefilledValue('center');
        }
    }

    private updateAcquEq(): void {
        // this.centerService.getCenter(this.acqEquip.center.id)
        //     .then(center => this.acqEquip.center = center);
        // this.manufModelService.getManufacturerModel(this.acqEquip.manufacturerModel.id)
        //     .then(manufModel => this.acqEquip.manufacturerModel = manufModel);
        this.datasetModalityTypeEnumValue = DatasetModalityType[this.acqEquip.manufacturerModel.datasetModalityType];
    }

    buildForm(): FormGroup {
        this.prefill();
        return this.formBuilder.group({
            'serialNumber': [this.acqEquip.serialNumber],
            'manufacturerModel': [this.acqEquip.manufacturerModel, Validators.required],
            'center': [{value: this.acqEquip.center, disabled: this.nonEditableCenter}, Validators.required], 
        });
    }

    private getManufModels(manufModelId?: number): void {
        this.manufModelService.getAll()
            .then(manufModels => this.manufModels = manufModels);
    }

    private openNewManufModel() {
        let currentStep: Step = this.breadcrumbsService.lastStep;
        this.router.navigate(['/manufacturer-model/create']).then(success => {
            currentStep.waitFor(this.breadcrumbsService.lastStep).subscribe(entity => {
                (currentStep.entity as AcquisitionEquipment).manufacturerModel = entity as ManufacturerModel;
            });
        });
    }

    private manageSaveErrors() {
        this.subscribtions.push(
            this.onSave.subscribe(response => {
                if (response && response instanceof ShanoirError && response.code == 422) {
                    this.uniqueSerialError = response.hasFieldError('manufacturerModel - serialNumber', 'unique'); 
                }
            })
        );
    }
}