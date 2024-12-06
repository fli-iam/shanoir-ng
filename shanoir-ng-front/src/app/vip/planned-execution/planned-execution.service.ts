/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2022 Inria - https://www.inria.fr/
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

import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import * as AppUtils from "../../utils/app.utils";
import {PlannedExecution} from "../models/planned-execution";
import {EntityService} from "../../shared/components/entity/entity.abstract.service";
import {BACKEND_API_VIP_PLANNED_EXEC_URL} from "../../utils/app.utils";

@Injectable()
export class PlannedExecutionService extends EntityService<PlannedExecution> {

    API_URL: string = AppUtils.BACKEND_API_VIP_PLANNED_EXEC_URL;

    constructor(protected httpClient: HttpClient) {super(httpClient);}

    getEntityInstance(entity: PlannedExecution | undefined): PlannedExecution {return new PlannedExecution();}

  /**
   * Get all planned executions linked to a study
   * @param study_id the study id we want the automatic executions from
   */
  public getPlannedExecutionsByStudy(study_id: number): Promise<PlannedExecution[]> {
    return this.httpClient.get<PlannedExecution[]>(this.API_URL + "/byStudy/" + study_id).toPromise();
  }
}
