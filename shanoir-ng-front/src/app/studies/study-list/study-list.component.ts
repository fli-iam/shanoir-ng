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

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { capitalsAndUnderscoresToDisplayable } from '../../utils/app.utils';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { UserService } from '../../users/shared/user.service';
import {StudyCardComponent} from "../../study-cards/study-card/study-card.component";


@Component({
    selector: 'study-list',
    templateUrl: 'study-list.component.html',
    styleUrls: ['study-list.component.css']
})

export class StudyListComponent extends BrowserPaginEntityListComponent<Study> {

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(
        private studyService: StudyService,
        private userService: UserService) {

        super('study');
    }

    getService(): EntityService<Study> {
        return this.studyService;
    }

    getEntities(): Promise<Study[]> {
        let earlyResult: Promise<Study[]> = Promise.all([
            this.studyService.getAll(true),
            this.studyService.getPublicStudiesData()
        ]).then(([studies, publicStudies]) => {
            if (!studies) studies = [];
            if (!publicStudies) publicStudies = [];
            studies = studies.concat(publicStudies
                .filter(publicStudy => !studies.find(s => s.id == publicStudy.id))
                .map(publicStudy => {
                    let study: Study = new Study();
                    study.id = publicStudy.id;
                    study.downloadableByDefault = publicStudy.downloadableByDefault;
                    study.endDate = publicStudy.endDate;
                    study.name = publicStudy.name;
                    study.nbExaminations = publicStudy.nbExaminations;
                    study.nbSubjects = publicStudy.nbSubjects;
                    study.startDate = publicStudy.startDate;
                    study.studyStatus = publicStudy.studyStatus;
                    study.studyType = publicStudy.studyType;
                    study.description = publicStudy.description;
                    study.studyTags = publicStudy.studyTags;
                    study.visibleByDefault = true;
                    study.locked = true;
                    return study;
                }));
            return studies;
        })
        Promise.all([
            earlyResult,
            this.userService.getAccessRequests()
        ]).then(([studies, accessRequests]) => {
            if (accessRequests?.length > 0) {
                for (let accessRequest of accessRequests) {
                    if (accessRequest.status == 0) {
                        studies.find(study => study.id == accessRequest.studyId).accessRequestedByCurrentUser = true;
                    }
                }
            }
        });
        return earlyResult;
    }

    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
            { headerName: "", type: "boolean", awesome: 'fa-solid fa-lock', awesomeFalse: 'fa-solid fa-lock-open', color: 'var(--color-a)', colorFalse: 'var(--color-a)',
                cellRenderer: ret => {
                    return (ret.data as Study).visibleByDefault && (ret.data as Study).locked;
                }
            },
            { headerName: "Name", field: "name" },
            {
                headerName: "Status", field: "studyStatus", width: '70px', cellRenderer: function (params: any) {
                    return capitalsAndUnderscoresToDisplayable(params.data.studyStatus);
                }
            },
            {
                headerName: "Start date", field: "startDate", type: "date", cellRenderer: (params: any) => {
                    return this.dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End date", field: "endDate", type: "date", cellRenderer: (params: any) => {
                    return this.dateRenderer(params.data.endDate);
                }
            },
            {
                headerName: "Subjects", field: "nbSujects", type: "number", width: '30px'
            },
            {
                headerName: "Examinations", field: "nbExaminations", type: "number", width: '30px'
            },
            {
                headerName: "Members", field: "nbMembers", type: "number", width: '30px'
            },
            {
                headerName: "Storage volume", field: "totalSize", disableSearch: true,
                tip: (study: any) => { return this.printDetailedStorageVolume(study.detailedSizes)},
                cellRenderer: (params: any) => { return this.studyService.storageVolumePrettyPrint(params.data.totalSize); }
            }
        ];
        return colDef;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    getOptions() {
        return {
            new: this.keycloakService.isUserAdminOrExpert(),
            view: true,
            edit: this.keycloakService.isUserAdminOrExpert(),
            delete: false
        };
    }

    canEdit(study: Study): boolean {
        return this.keycloakService.isUserAdmin() || (
            study.studyUserList &&
            study.studyUserList.filter(su => su.studyUserRights.includes(StudyUserRight.CAN_ADMINISTRATE)).length > 0
        );
    }

    goToViewFromEntity(study: any): void {
        if (study instanceof Study) {
            if (study.visibleByDefault && study.locked && !this.keycloakService.isUserAdmin()) {
                if (study.accessRequestedByCurrentUser) {
                    this.confirmDialogService.inform('Access request pending', 'You already have asked an access request for this study, wait for the administrator to confirm your access.');
                } else {
                    this.confirmDialogService.confirm('Authorization needed',
                        'Before accessing this study you have to request an access to its administrator, do you want to proceed ?'
                    ).then(result => {
                        if (result) this.router.navigate(['/access-request/study/' + study.id]);
                    });
                }
            } else {
                super.goToViewFromEntity(study);
            }
        }
    }

    private printDetailedStorageVolume(detailedSizes: Map<String, number>) {
        if(!detailedSizes){
            return "";
        }
        let detail = "";
        let sortedSizes = new Map([...detailedSizes.entries()].sort((a, b) => b[1] - a[1]));
        sortedSizes.forEach((size: number, format: String) => {
            detail += format + " : " + this.studyService.storageVolumePrettyPrint(size) + "\n";
        });
        return detail;
    }
}
