import { Center } from '../../centers/shared/center.model';
import { ManufacturerModel } from './manufacturer-model.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { AcquisitionEquipmentService } from './acquisition-equipment.service';
import { ServiceLocator } from '../../utils/locator.service';

export class AcquisitionEquipment extends Entity {
    id: number;
    serialNumber: string;
    center: Center;
    manufacturerModel: ManufacturerModel;

    service: AcquisitionEquipmentService = ServiceLocator.injector.get(AcquisitionEquipmentService);
}