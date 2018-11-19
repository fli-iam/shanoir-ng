import { AnestheticType } from "../../../shared/enum/anestheticType";
import { AnestheticIngredient } from '../../ingredients/shared/anestheticIngredient.model';
import { ServiceLocator } from "../../../../utils/locator.service";
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { AnestheticService } from "./anesthetic.service";

export class Anesthetic extends Entity{
  id: number;
  name: string;
  comment: string;
  anestheticType: AnestheticType;
  ingredients: AnestheticIngredient[];

  service: AnestheticService = ServiceLocator.injector.get(AnestheticService);
}

