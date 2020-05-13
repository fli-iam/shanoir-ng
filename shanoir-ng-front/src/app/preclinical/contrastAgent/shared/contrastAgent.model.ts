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

import { Reference } from '../../reference/shared/reference.model';
import { ContrastAgentService } from './contrastAgent.service';
import { ServiceLocator } from "../../../utils/locator.service";
import { Entity } from "../../../shared/components/entity/entity.abstract";

export class ContrastAgent extends Entity{
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

  service: ContrastAgentService = ServiceLocator.injector.get(ContrastAgentService);
}

