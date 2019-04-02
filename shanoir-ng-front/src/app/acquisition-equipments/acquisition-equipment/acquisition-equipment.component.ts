import { Component } from '@angular/core';
import { AbstractControl, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { CenterService } from '../../centers/shared/center.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { DatasetModalityType } from '../../shared/enums/dataset-modality-type';
import { IdNameObject } from '../../shared/models/id-name-object.model';
import { AcquisitionEquipment } from '../shared/acquisition-equipment.model';
import { AcquisitionEquipmentService } from '../shared/acquisition-equipment.service';
import { ManufacturerModel } from '../shared/manufacturer-model.model';
import { ManufacturerModelService } from '../shared/manufacturer-model.service';

@Component({
    selector: 'acquisition-equipment-detail',
    templateUrl: 'acquisition-equipment.component.html'
})

export class AcquisitionEquipmentComponent extends EntityComponent<AcquisitionEquipment> {

    private manufModels: ManufacturerModel[];
    private centers: IdNameObject[];
    private datasetModalityTypeEnumValue: string;
    private nonEditableCenter: boolean = false;
    private lastSubmittedManufAndSerial: ManufacturerAndSerial;

    private get acqEquip(): AcquisitionEquipment { return this.entity; }
    private set acqEquip(acqEquip: AcquisitionEquipment) { this.entity = acqEquip; }
    
    constructor(
            private route: ActivatedRoute, 
            private acqEquipService: AcquisitionEquipmentService, 
            private manufModelService: ManufacturerModelService,
            private centerService: CenterService) {

        super(route, 'acquisition-equipment');
    }

    async initView(): Promise<void> {
        this.acqEquip = await this.acqEquipService.get(this.id);
        this.updateAcquEq();
    }

    async initEdit(): Promise<void> {
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
        this.nonEditableCenter = this.breadcrumbsService.currentStep.isPrefilled('center');
        if (this.nonEditableCenter) {
            this.acqEquip.center = this.breadcrumbsService.currentStep.getPrefilledValue('center');
        }
    }

    private updateAcquEq(): void {
        this.datasetModalityTypeEnumValue = DatasetModalityType[this.acqEquip.manufacturerModel.datasetModalityType];
    }

    buildForm(): FormGroup {
        this.prefill();
        let form: FormGroup = this.formBuilder.group({
            'serialNumber': [this.acqEquip.serialNumber, [this.manufAndSerialUnicityValidator]],
            'manufacturerModel': [this.acqEquip.manufacturerModel, [Validators.required]],
            'center': [{value: this.acqEquip.center, disabled: this.nonEditableCenter}, Validators.required], 
        });
        this.registerManufAndSerialUnicityValidator(form);
        return form;
    }

    private getManufModels(manufModelId?: number): void {
        this.manufModelService.getAll()
            .then(manufModels => this.manufModels = manufModels);
    }

    private openNewManufModel() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/manufacturer-model/create']).then(success => {
            currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                (currentStep.entity as AcquisitionEquipment).manufacturerModel = entity as ManufacturerModel;
            });
        });
    }

    private registerManufAndSerialUnicityValidator(form: FormGroup) {
        this.onSubmitValidatedFields.push('serialNumber');
        form.get('manufacturerModel').valueChanges.subscribe(value => {
            form.get('serialNumber').updateValueAndValidity();
        })
    }

    private manufAndSerialUnicityValidator = (control: AbstractControl): ValidationErrors | null => {
        if (this.saveError && this.saveError.hasFieldError('manufacturerModel - serialNumber', 'unique')
                && this.acqEquip.manufacturerModel.id == this.lastSubmittedManufAndSerial.manuf.id
                && this.acqEquip.serialNumber == this.lastSubmittedManufAndSerial.serial) {       
            return {unique: true};
        }
        return null;
    }

    save(): Promise<void> {
        this.lastSubmittedManufAndSerial = new ManufacturerAndSerial(this.acqEquip.manufacturerModel, this.acqEquip.serialNumber);
        return super.save();
    }
}

export class ManufacturerAndSerial {
    constructor(
        public manuf: ManufacturerModel,
        public serial: string
    ) {}
}

