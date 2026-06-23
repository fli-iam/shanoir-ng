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
import { DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { DatepickerComponent } from "src/app/shared/date-picker/date-picker.component";

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { StudyService } from "../../studies/shared/study.service";

import { AccessRequest } from './access-request.model';
import { AccessRequestService } from './access-request.service';

@Component({
    selector: 'accessRequestList',
    templateUrl: 'access-request-list.component.html',
    styleUrls: ['access-request-list.component.css'],
    imports: [FormsModule, RouterLink, DatePipe, DatepickerComponent]
})

export class AccessRequestListComponent {
    accessRequests: AccessRequest[] = [];

    constructor(
            public accessRequestService: AccessRequestService,
            public studyService: StudyService,
            public breadcrumbsService: BreadcrumbsService,
            public keycloakService: KeycloakService) {

        this.accessRequestService.getAccessRequestsForAdmin().then(accessRequests => {
            this.accessRequests = [...accessRequests];
        });

        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Access Requests';
        });
    }

    get studyAccessRequests(): AccessRequest[] {
        return this.accessRequests.filter(ar => !ar.user?.accountRequestDemand);
    }

    get accountCreationRequests(): AccessRequest[] {
        return this.accessRequests.filter(ar => ar.user?.accountRequestDemand);
    }

    decide(request: AccessRequest, accept: boolean) {
        this.accessRequestService.resolveRequest(request.id, accept, request.expiration);
        const index = this.accessRequests.indexOf(request);
        this.accessRequests.splice(index, 1);
        this.accessRequestService.decreaseAccessRequests();
    }

    public isAdmin(): boolean {
        return this.keycloakService.isUserAdmin();
    }
}
