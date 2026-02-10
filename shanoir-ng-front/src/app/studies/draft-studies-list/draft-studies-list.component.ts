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
import { Component, ViewChild } from '@angular/core';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { TableComponent } from '../../shared/components/table/table.component';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';


@Component({
    selector: 'draft-studies-list',
    templateUrl: 'draft-studies-list.component.html',
    styleUrls: ['draft-studies-list.component.css'],
    standalone: false
})

export class DraftStudiesListComponent {

    draftStudies: Study[] = []

    constructor(
        private studyService: StudyService,
        private breadcrumbsService: BreadcrumbsService) {

        studyService.getStudiesByDraftState().then(draftStudies => this.draftStudies = draftStudies);
        setTimeout(() => {
            breadcrumbsService.currentStepAsMilestone();
            breadcrumbsService.currentStep.label = 'Draft Studies';
        });
    }

    decide(index: number) {
        this.studyService.toggleDraftStateById(this.draftStudies[index].id);
        this.draftStudies.splice(index, 1);
        this.studyService.decreaseDraftStudies();
    }
}
