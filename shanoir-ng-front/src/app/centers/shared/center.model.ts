import { AcquisitionEquipment } from "../../acquisition-equipments/shared/acquisition-equipment.model";
import { Entity } from "../../shared/components/entity/entity.interface";
import { CenterService } from "./center.service";
import { ServiceLocator } from "../../utils/locator.service";

export class Center implements Entity {
    acquisitionEquipments: AcquisitionEquipment[];
    city: string;
    country: string;
    id: number;
    name: string;
    phoneNumber: string;
    postalCode: string;
    street: string;
    website: string;


    private service: CenterService = ServiceLocator.injector.get(CenterService);

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