import { Reference } from '../../../reference/shared/reference.model';
import { Anesthetic } from '../../anesthetic/shared/anesthetic.model';
import { InjectionType } from "../../../shared/enum/injectionType";
import { InjectionInterval } from "../../../shared/enum/injectionInterval";
import { InjectionSite } from "../../../shared/enum/injectionSite";
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { ExaminationAnestheticService } from "./examinationAnesthetic.service";
import { ServiceLocator } from "../../../../utils/locator.service";

export class ExaminationAnesthetic extends Entity {
  id: number;
  examination_id: number;
  anesthetic:Anesthetic;
  dose:number;
  dose_unit:Reference;
  injection_interval: InjectionInterval;
  injection_site: InjectionSite;
  injection_type: InjectionType;
  startDate: Date;
  endDate : Date;

  service: ExaminationAnestheticService = ServiceLocator.injector.get(ExaminationAnestheticService);

}

