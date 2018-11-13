import { AcquisitionEquipment } from "../../acquisition-equipments/shared/acquisition-equipment.model";
import { Entity } from "../../shared/components/entity/entity.abstract";
import { CenterService } from "./center.service";
import { ServiceLocator } from "../../utils/locator.service";

export class Center extends Entity {
    acquisitionEquipments: AcquisitionEquipment[];
    city: string;
    country: string;
    id: number;
    name: string;
    phoneNumber: string;
    postalCode: string;
    street: string;
    website: string;
    compatible: boolean;

    service: CenterService = ServiceLocator.injector.get(CenterService);
}