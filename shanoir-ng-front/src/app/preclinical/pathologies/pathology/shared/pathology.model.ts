import { PathologyService } from "./pathology.service";
import { ServiceLocator } from "../../../../utils/locator.service";
import { Entity } from "../../../../shared/components/entity/entity.abstract";

export class Pathology  extends Entity {
  id: number;
  name: string;

  service: PathologyService = ServiceLocator.injector.get(PathologyService);
}

