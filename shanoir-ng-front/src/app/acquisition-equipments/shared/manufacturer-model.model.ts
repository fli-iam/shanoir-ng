import { Entity } from '../../shared/components/entity/entity.abstract';
import { ServiceLocator } from '../../utils/locator.service';
import { ManufacturerModelService } from './manufacturer-model.service';
import { Manufacturer } from './manufacturer.model';

export class ManufacturerModel extends Entity {
    id: number;
    name: string;
    manufacturer: Manufacturer;
    magneticField: number;
    datasetModalityType: string;

    service: ManufacturerModelService = ServiceLocator.injector.get(ManufacturerModelService);
}