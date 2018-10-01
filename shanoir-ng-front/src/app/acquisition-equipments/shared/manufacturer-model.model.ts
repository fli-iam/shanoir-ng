import { Entity } from '../../shared/components/entity/entity.interface';
import { ServiceLocator } from '../../utils/locator.service';
import { ManufacturerModelService } from './manufacturer-model.service';
import { Manufacturer } from './manufacturer.model';

export class ManufacturerModel implements Entity {
    id: number;
    name: string;
    manufacturer: Manufacturer;
    magneticField: number;
    datasetModalityType: string;

    private service: ManufacturerModelService = ServiceLocator.injector.get(ManufacturerModelService);

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