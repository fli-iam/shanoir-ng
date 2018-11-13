import { TherapyType } from '../../../shared/enum/therapyType';
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { TherapyService } from "./therapy.service";
import { ServiceLocator } from "../../../../utils/locator.service";

export class Therapy extends Entity{
  id: number;
  name: string;
  comment: string;
  therapyType: TherapyType;

  service: TherapyService = ServiceLocator.injector.get(TherapyService);
}

