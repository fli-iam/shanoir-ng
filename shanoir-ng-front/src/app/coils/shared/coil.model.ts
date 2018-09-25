import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { Center } from '../../centers/shared/center.model';
import { Entity } from '../../shared/components/entity/entity.interface';
import { ServiceLocator } from '../../utils/locator.service';
import { CoilType } from './coil-type.enum';
import { CoilService } from './coil.service';

export class Coil implements Entity {

    id: number;
    name: string;
    numberOfChannels: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;
    coilType:CoilType;

    
    private coilService: CoilService = ServiceLocator.injector.get(CoilService);

    create(): Promise<Entity> {
        return this.coilService.create(this);
    }

    update(): Promise<void> {
        return this.coilService.update(this.id, this);
    }

    delete(): Promise<void> {
        return this.coilService.delete(this.id);
    }
}