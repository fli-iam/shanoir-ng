/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

 import { ServiceLocator } from 'src/app/utils/locator.service';
import { Reference } from '../../../reference/shared/reference.model';
import { Anesthetic } from '../../anesthetic/shared/anesthetic.model';
import { InjectionType } from "../../../shared/enum/injectionType";
import { InjectionInterval } from "../../../shared/enum/injectionInterval";
import { InjectionSite } from "../../../shared/enum/injectionSite";
import { Entity } from "../../../../shared/components/entity/entity.abstract";
import { ExaminationAnestheticService } from "./examinationAnesthetic.service";

export class ExaminationAnesthetic extends Entity {
  id: number;
  internal_id: number;
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

