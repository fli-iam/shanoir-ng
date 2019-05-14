import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { Center } from '../../centers/shared/center.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { Id } from '../../shared/models/id.model';
import { ServiceLocator } from '../../utils/locator.service';
import { CoilType } from './coil-type.enum';
import { CoilService } from './coil.service';

export class Coil extends Entity {

    id: number;
    name: string;
    numberOfChannels: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;
    coilType:CoilType;

    service: CoilService = ServiceLocator.injector.get(CoilService);

    // Override
    public stringify() {
        return JSON.stringify(new CoilDTO(this), this.replacer);
    }
}

export class CoilDTO {

    id: number;
    name: string;
    numberOfChannels: number;
    serialNumber: string;
    center: Id;
    manufacturerModel: Id;
    coilType: CoilType;

    constructor(coil: Coil) {
        this.id = coil.id;
        this.name = coil.name;
        this.numberOfChannels = coil.numberOfChannels;
        this.serialNumber = coil.serialNumber;
        this.center = new Id(coil.center.id);
        this.manufacturerModel = new Id(coil.manufacturerModel.id);
        this.coilType = coil.coilType;
    }
}