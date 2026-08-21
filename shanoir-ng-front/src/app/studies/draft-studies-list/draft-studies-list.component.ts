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
import { RouterLink } from '@angular/router';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';


@Component({
    selector: 'draft-studies-list',
    templateUrl: 'draft-studies-list.component.html',
    styleUrls: ['draft-studies-list.component.css'],
    imports: [RouterLink]
})

export class DraftStudiesListComponent {

    draftStudies: Study[] = []

    constructor(
        private studyService: StudyService,
        private keycloakService: KeycloakService,
        private breadcrumbsService: BreadcrumbsService) {

        if (this.keycloakService.isUserAdmin())
            studyService.findDraftStudies().then(draftStudies => this.draftStudies = draftStudies);
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Draft Studies';
        });
    }

    async decide(index: number) {
        const approved = await this.studyService.approveStudyById(this.draftStudies[index].id);
        if (!approved) return;
        this.draftStudies.splice(index, 1);
        this.studyService.decreaseDraftStudies();
    }
}
