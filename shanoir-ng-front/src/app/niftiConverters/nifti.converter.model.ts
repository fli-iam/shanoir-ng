import { Entity } from "../shared/components/entity/entity.abstract";
import { ServiceLocator } from "../utils/locator.service";
import { NiftiConverterService } from "./nifti.converter.service";

export class NiftiConverter extends Entity{
    id: number;
    name: string;
    isActive: boolean;
    // TODO: add version and type(enum)

    service: NiftiConverterService = ServiceLocator.injector.get(NiftiConverterService);
}