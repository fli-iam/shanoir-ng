import { Reference } from '../../../reference/shared/reference.model';
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { AnestheticIngredientService } from "./anestheticIngredient.service";
import { ServiceLocator } from "../../../../utils/locator.service";

export class AnestheticIngredient extends Entity {
  id: number;
  name: Reference;
  concentration: number;
  concentration_unit: Reference;

  service: AnestheticIngredientService = ServiceLocator.injector.get(AnestheticIngredientService);
}

