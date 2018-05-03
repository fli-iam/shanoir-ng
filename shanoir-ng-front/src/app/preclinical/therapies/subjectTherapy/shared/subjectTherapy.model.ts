import { Reference } from '../../../reference/shared/reference.model';
import { Therapy } from '../../therapy/shared/therapy.model';
import { Frequency } from "../../../shared/enum/frequency";

export class SubjectTherapy{
  id: number;
  therapy: Therapy;
  dose: number;
  dose_unit: Reference;
  frequency: Frequency;
  startDate: Date;
  endDate : Date;
}

