import { Reference } from '../../../reference/shared/reference.model';

export class AnestheticIngredient {
  id: number;
  name: Reference;
  concentration: number;
  concentration_unit: Reference;
}

