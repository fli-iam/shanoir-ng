import { Reference } from '../../reference/shared/reference.model';

export class ContrastAgent {
  id: number;
  name: Reference;
  manufactured_name: string;
  concentration: number;
  concentration_unit: Reference;
  dose: number;
  dose_unit: Reference;
  injection_interval: Reference;
  injection_site: Reference;
  injection_type: Reference;
}

