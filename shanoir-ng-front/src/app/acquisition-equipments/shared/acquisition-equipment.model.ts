import { Center } from '../../centers/shared/center.model';
import { ManufacturerModel } from './manufacturer-model.model';
import { Entity } from '../../shared/components/entity/entity.interface';
import { AcquisitionEquipmentService } from './acquisition-equipment.service';
import { ServiceLocator } from '../../utils/locator.service';

export class AcquisitionEquipment implements Entity {
    id: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;

    private service: AcquisitionEquipmentService = ServiceLocator.injector.get(AcquisitionEquipmentService);

    create(): Promise<Entity> {
        return this.service.create(this);
    }

    update(): Promise<void> {
        return this.service.update(this.id, this);
    }

    delete(): Promise<void> {
        return this.service.delete(this.id);
    }
}