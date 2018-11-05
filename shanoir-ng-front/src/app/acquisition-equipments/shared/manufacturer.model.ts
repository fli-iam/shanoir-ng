import { Entity } from "../../shared/components/entity/entity.abstract";
import { ManufacturerService } from "./manufacturer.service";
import { ServiceLocator } from "../../utils/locator.service";

export class Manufacturer extends Entity {
    id: number;
    name: String;

    service: ManufacturerService = ServiceLocator.injector.get(ManufacturerService);
}