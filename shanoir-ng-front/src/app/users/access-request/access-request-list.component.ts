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
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { disapearUp } from '../../shared/animations/animations';
import { UserService } from '../shared/user.service';

import { AccessRequest } from './access-request.model';
import { AccessRequestService } from './access-request.service';

@Component({
    selector: 'accessRequestList',
    templateUrl: 'access-request-list.component.html',
    styleUrls: ['access-request-list.component.css'],
    animations: [disapearUp],
    imports: [FormsModule, RouterLink]
})

export class AccessRequestListComponent {

    accessRequests: AccessRequest[] = [];

    constructor(
            public userService: UserService,
            public accessRequestService: AccessRequestService,
            public breadcrumbsService: BreadcrumbsService) {

        userService.getAccessRequestsForAdmin().then(accessRequests => this.accessRequests = accessRequests);
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Acces Requests';
        });
    }

    decide(index: number, accept: boolean) {
        this.accessRequestService.resolveRequest(this.accessRequests[index].id, accept);
        this.accessRequests.splice(index, 1);
        this.userService.decreaseAccessRequests();
    }
}
