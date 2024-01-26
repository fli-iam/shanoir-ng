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

import { APP_ID, Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { ExecutionMonitoring } from "../models/execution-monitoring.model";
import * as AppUtils from "../../utils/app.utils";
import { Observable } from "rxjs";
import { EntityService } from "src/app/shared/components/entity/entity.abstract.service";


@Injectable()
export class ExecutionMonitoringService extends EntityService<ExecutionMonitoring>{

    API_URL: string = AppUtils.BACKEND_API_VIP_EXEC_MONITORING_URL;

    constructor(protected httpClient: HttpClient) {
        super(httpClient);
    }

    getEntityInstance() { return new ExecutionMonitoring(); }

    public getAllExecutionMonitorings(): Observable<ExecutionMonitoring[]>{
        return this.httpClient.get<ExecutionMonitoring[]>(`${this.API_URL}/all`);
    }

    public getExecutionMonitoring(id: number): Observable<ExecutionMonitoring>{
        return this.httpClient.get<ExecutionMonitoring>(`${this.API_URL}/${id}`)
    }

    public updateAndStart(monitoring: ExecutionMonitoring){
        return this.http.put<any>(this.API_URL + '/' + monitoring.id + '?start=true', this.stringify(monitoring), {reportProgress: true,
            observe: 'events'})
            .toPromise();
    }

}
