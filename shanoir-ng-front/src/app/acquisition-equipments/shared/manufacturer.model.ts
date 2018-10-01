import { Entity } from "../../shared/components/entity/entity.interface";
import { ManufacturerService } from "./manufacturer.service";
import { ServiceLocator } from "../../utils/locator.service";

export class Manufacturer implements Entity {
    id: number;
    name: String;

    private service: ManufacturerService = ServiceLocator.injector.get(ManufacturerService);

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