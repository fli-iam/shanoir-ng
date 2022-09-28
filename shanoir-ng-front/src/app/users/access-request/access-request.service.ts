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
import { HttpClient, HttpEvent, HttpEventType, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { ErrorHandler, Injectable, OnDestroy } from '@angular/core';
import { saveAs } from 'file-saver';
import { Subscription } from 'rxjs';
import { Observable } from 'rxjs/Observable';

import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { LoadingBarComponent } from '../../shared/components/loading-bar/loading-bar.component';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import * as AppUtils from '../../utils/app.utils';
import { AccessRequest } from './access-request.model';
import { ServiceLocator } from '../../utils/locator.service';

@Injectable()
export class AccessRequestService extends EntityService<AccessRequest> implements OnDestroy {

    getEntityInstance(entity?: AccessRequest): AccessRequest {
        return new AccessRequest();
    }

    API_URL = AppUtils.BACKEND_API_USER_ACCESS_REQUEST;

    subscribtions: Subscription[] = [];
    
    constructor(protected http: HttpClient) {
        super(http);
    }

    ngOnDestroy() {
        for(let subscribtion of this.subscribtions) {
            subscribtion.unsubscribe();
        }
    }
}