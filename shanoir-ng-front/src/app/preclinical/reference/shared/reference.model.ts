import { Entity } from "../../../shared/components/entity/entity.abstract";
import { ReferenceService } from "./reference.service";
import { ServiceLocator } from "../../../utils/locator.service";

export class Reference extends Entity{
  id: number;
  category: string;
  reftype: string;
  value: string;

  service: ReferenceService = ServiceLocator.injector.get(ReferenceService);
}

