import { Reference } from '../../../reference/shared/reference.model';
import { Therapy } from '../../therapy/shared/therapy.model';
import { Frequency } from "../../../shared/enum/frequency";
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { ServiceLocator } from "../../../../utils/locator.service";
import { SubjectTherapyService } from './subjectTherapy.service';

export class SubjectTherapy  extends Entity{
  id: number;
  therapy: Therapy;
  dose: number;
  dose_unit: Reference;
  frequency: Frequency;
  startDate: Date;
  endDate : Date;

  service: SubjectTherapyService = ServiceLocator.injector.get(SubjectTherapyService);
}

