import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { Center } from '../../centers/shared/center.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
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
}