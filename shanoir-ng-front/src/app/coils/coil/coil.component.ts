import { Component, Input } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { Center } from '../../centers/shared/center.model';
import { CenterService } from '../../centers/shared/center.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { CoilType } from '../shared/coil-type.enum';
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';
import { CenterComponent } from '../../centers/center/center.component';
import { ManufacturerComponent } from '../../acquisition-equipments/manufacturer/manufacturer.component';
import { ModalService } from '../../shared/components/modals/modal.service';

@Component({
    selector: 'coil',
    templateUrl: 'coil.component.html',
    styleUrls: ['coil.component.css']
})
export class CoilComponent extends EntityComponent<Coil> {
   
    @Input() acqEquip: AcquisitionEquipment;
    private centers: Center[] = [];
    private manufModels: ManufacturerModel[] = [];
    private coilTypes: CoilType[] = CoilType.all();

    constructor(
            private route: ActivatedRoute,
            private coilService: CoilService, 
            private centerService: CenterService,
            private modalService: ModalService) {
        super(route, 'coil');
    }

    get coil(): Coil {
        return this.entity;
    }

    set coil(coil: Coil) {
        this.entity = coil;
    }

    initView(): Promise<void> {
        return this.coilService.getCoil(this.id).then(coil => {
            this.coil = coil;
        });
    }

    initEdit(): Promise<void> {
        return Promise.all([
            this.centerService.getCenters(),
            this.coilService.getCoil(this.id)
        ]).then(([centers, coil]) => {
            this.centers = centers;
            this.coil = coil;
            if (this.acqEquip) {
                coil.center = this.acqEquip.center;
                coil.manufacturerModel = this.acqEquip.manufacturerModel;
            }
            this.coil.center = this.centers.find(center => center.id == this.coil.center.id);
            this.updateManufList(this.coil.center);
            this.coil.manufacturerModel = this.manufModels.find(manuf => manuf.id == this.entity.manufacturerModel.id);
        });
    }

    initCreate(): Promise<void> {
        this.entity = new Coil();
        return this.centerService.getCenters().then(centers => {
            this.centers = centers;
        });
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.coil.name],
            'acquiEquipModel': [this.coil.manufacturerModel],
            'center': [this.coil.center],
            'coilType': [this.coil.coilType],
            'nbChannel': [this.coil.numberOfChannels],
            'serialNb': [this.coil.serialNumber]
        });
    }
    

    private updateManufList(center: Center): void {
        this.manufModels = [];
        if (center) center.acquisitionEquipments.map(acqEqu => this.manufModels.push(acqEqu.manufacturerModel));
    }

    private openNewCenter() {
        this.modalService.open(CenterComponent, 'create').then(
            (newCenter) => {
                this.centers = [newCenter].concat(this.centers);
                this.coil.center = newCenter;
                this.updateManufList(this.coil.center);
            }
        );
    }

    private openNewManufModel() {
        this.modalService.open(ManufacturerComponent, 'create').then(
            (newManuf) => {
                this.manufModels = [newManuf].concat(this.manufModels);
            }
        );
    }

}