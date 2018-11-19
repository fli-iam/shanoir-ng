import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { ServiceLocator } from "../../../../utils/locator.service";
import { ExtraDataService } from "./extradata.service";

export class ExtraData extends Entity{
  id: number;
  examination_id: number;
  filename: string;
  extradatatype:string;

  service: ExtraDataService = ServiceLocator.injector.get(ExtraDataService);
}

