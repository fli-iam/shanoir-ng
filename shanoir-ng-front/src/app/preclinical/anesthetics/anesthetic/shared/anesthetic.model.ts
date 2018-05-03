import { AnestheticType } from "../../../shared/enum/anestheticType";
import { AnestheticIngredient } from '../../ingredients/shared/anestheticIngredient.model';

export class Anesthetic {
  id: number;
  name: string;
  comment: string;
  anestheticType: AnestheticType;
  ingredients: AnestheticIngredient[];
}

