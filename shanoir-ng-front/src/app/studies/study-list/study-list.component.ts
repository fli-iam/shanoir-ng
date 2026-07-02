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
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { AccessRequestService } from 'src/app/users/access-request/access-request.service';
import { Filter } from 'src/app/shared/components/table/pageable.model';

import { DatasetExpressionFormat } from "../../enum/dataset-expression-format.enum";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { TableComponent } from '../../shared/components/table/table.component';
import { capitalsAndUnderscoresToDisplayable } from '../../utils/app.utils';
import { StudyUserRight } from '../shared/study-user-right.enum';
import { StudyUser } from "../shared/study-user.model";
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { StudyLight } from '../shared/study.dto';


@Component({
    selector: 'study-list',
    templateUrl: 'study-list.component.html',
    styleUrls: ['study-list.component.css'],
    imports: [TableComponent]
})

export class StudyListComponent extends BrowserPaginEntityListComponent<Study> {

    @ViewChild('table', { static: false }) table: TableComponent;
    private studyIdsForCurrentUser: number[];
    private requestDates: Map<number, Date>;
    protected tableFilter: Filter;

    constructor(
        private studyService: StudyService,
        private confirmService: ConfirmDialogService,
        private accessRequestService: AccessRequestService,
        private activatedRoute: ActivatedRoute) {

        super('study');
        this.studyService.getStudiesByRight(StudyUserRight.CAN_ADMINISTRATE).then( studies => this.studyIdsForCurrentUser = studies);
        this.studyService.fetchCurrentUserStudyDates().then(requestDates => {
            this.requestDates = requestDates;
        });
    }

    ngOnInit() {
        super.ngOnInit();
        const id: string = this.activatedRoute.snapshot.queryParamMap.get('id');
        if (id && !isNaN(parseInt(id))) {
            this.tableFilter = new Filter(id, 'id');
        }
        const extensionRequestId: string = this.activatedRoute.snapshot.queryParamMap.get('requestExtension');
        if (extensionRequestId && !isNaN(parseInt(extensionRequestId))) {
            const studyId = parseInt(extensionRequestId);
            this.entitiesPromise.then(studies => {
                const study: Study = studies?.find(s => s.id == studyId);
                if (study) {
                    const userId: number = this.keycloakService.getUserId();
                    const studyUser: StudyUser = study.studyUserList?.find(su => su.userId == userId);
                    const currentExpirationDate: Date = studyUser?.expiration;
                    this.accessRequestService.openAccessExtensionModal(currentExpirationDate, study);
                }
            });
        }
    }

    getService(): EntityService<Study> {
        return this.studyService;
    }

    getEntities(eager: boolean = false): Promise<Study[]> {
        const earlyResult: Promise<Study[]> = Promise.all([
            this.studyService.getAll().then(studies => this.fetchStorageVolumesByChunk(studies)),
            this.studyService.getPublicStudiesData(),
            this.studyService.getExpiredStudiesData()
        ]).then(([studies, publicStudies, expiredStudies]) => {
            if (!studies) studies = [];
            if (!publicStudies) publicStudies = [];
            if (!expiredStudies) expiredStudies = [];
            studies = studies.concat(publicStudies
                .filter(publicStudy => !studies.find(s => s.id == publicStudy.id))
                .map(publicStudy => {
                    const study: Study = StudyLight.toStudy(publicStudy);
                    study.visibleByDefault = true;
                    study.locked = true;
                    return study;
                }));
            studies = studies.concat(expiredStudies
                .map(expiredStudy => {
                    const study: Study = StudyLight.toStudy(expiredStudy);
                    study.visibleByDefault = true;
                    study.locked = true;
                    return study;
                }));
            return studies;
        });
        const allPromise: Promise<Study[]> = Promise.all([
            earlyResult,
            this.accessRequestService.getAccessRequests()
        ]).then(([studies, accessRequests]) => {
            if (accessRequests?.length > 0) {
                for (const accessRequest of accessRequests) {
                    if (accessRequest.status == 0) {
                        studies.find(study => study.id == accessRequest.studyId).accessRequestedByCurrentUser = true;
                    }
                }
            }
            return studies;
        });
        if (eager) {
            return allPromise;
        } else {
            return earlyResult;
        }
    }

    private fetchStorageVolumesByChunk(studies) {
        if (studies) {
            const chunkSize = 10;
            const chunks: Study[][] = [];
            for (let i = 0; i < studies.length; i += chunkSize) {
                const chunk = studies.slice(i, i + chunkSize);
                chunks.push(chunk);
            }
            let queue: Promise<void> = Promise.resolve();
            chunks.forEach(chunk => {
                queue = queue.then(() => this.fetchStorageVolumes(chunk));
            });
            queue.then(() => {
                this.columnDefs.forEach(column => {
                    if (column.headerName === "Storage volume") {
                        column.disableSorting = false;
                    }
                })
            });
            return studies;
        }
    }

    private fetchStorageVolumes(studies: Study[]): Promise<void> {
        return this.studyService.getStudiesStorageVolume(studies?.map(s => s.id)).then(volumes => {
            studies.forEach(study => {
                const volume = volumes.get(study.id);
                if(volume) {
                    (study as Study).totalSize = volume.total;
                    const sizesByLabel = new Map<string, number>()
                    if (volume.volumeByFormat) {
                        for (const sizeByFormat of volume.volumeByFormat) {
                            if (sizeByFormat.size > 0) {
                                sizesByLabel.set(DatasetExpressionFormat.getLabel(sizeByFormat.format), sizeByFormat.size);
                            }
                        }
                    }
                    if (volume.extraDataSize && volume.extraDataSize > 0) {
                        sizesByLabel.set("Other files (DUA, protocol...)", volume.extraDataSize);
                    }
                    (study as Study).detailedSizes = sizesByLabel;
                }
            });
        });
    }

    getColumnDefs(): ColumnDefinition[] {
        const colDef: ColumnDefinition[] = [
            { headerName: "", type: "boolean", awesome: 'fa-solid fa-lock', awesomeFalse: 'fa-solid fa-lock-open', color: 'var(--color-a)', colorFalse: 'var(--color-a)',
                cellRenderer: ret => {
                    return (ret.data as Study).visibleByDefault && (ret.data as Study).locked;
                }
            },
            {
                headerName: "", type: "boolean", field: "isDraft", awesome: 'fa-regular fa-clipboard', color: 'var(--color-a)', width: '15px',
                tip: ret => ret.isDraft ? "draft" : null
            },
            { headerName: "Name", field: "name" },
            {
                headerName: "Status", field: "studyStatus", width: '70px', cellRenderer: function (params: any) {
                    return capitalsAndUnderscoresToDisplayable(params.data.studyStatus);
                }
            },
            { headerName: "Start date", field: "startDate", type: "date" },
            { headerName: "End date", field: "endDate", type: "date" },
            {
                headerName: "Subjects", field: "nbSubjects", type: "number", width: '30px'
            },
            {
                headerName: "Examinations", field: "nbExaminations", type: "number", width: '30px'
            },
            {
                headerName: "Members", field: "nbMembers", type: "number", width: '30px'
            },
            {
                headerName: "Storage volume", field: "totalSize", disableSearch: true, disableSorting: true, type: "number", orderBy: ["totalSize"],
                cellRenderer: (params: any) => {
                    if (params.data?.totalSize >= 0) {
                        return this.studyService.storageVolumePrettyPrint(params.data.totalSize);
                    } else if (params.data.locked) {
                        return "";
                    } else {
                        return "Fetching..."
                    }
                },
                tip: (data: any) => {
                    let tip = ""
                    if(data.detailedSizes){
                        data.detailedSizes.forEach((size: number, label: string) => {
                            tip += label + " : " + this.studyService.storageVolumePrettyPrint(size) + "\n";
                        });
                        return tip;
                    } else {
                        return "Calculating the detailed study storage volume, this may take up to a minute"
                    }
                }
            }, {
                headerName: "Access end", type: "date",
                cellRenderer: (params: any) => {
                    return this.requestDates?.get(params.data?.id);
                },
                cellGraphics: (data: any) => {
                    const maxDate = this.requestDates?.get(data.id);
                    if (maxDate && maxDate > new Date()) {
                        if (maxDate && maxDate < new Date(new Date().getTime() + 30 * 24 * 60 * 60 * 1000)) {
                            return {color: 'darkorange'};
                        } else {
                            return {color: 'green'};
                        }
                    } else {
                        return {color: 'red'};
                    }
                }
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

    greyedCondition(study: Study): boolean {
        return study?.isDraft;
    }

    canEdit(study: Study): boolean {
        return this.keycloakService.isUserAdmin() || this.studyIdsForCurrentUser.includes(study.id);
    }

    private fetchStudyUsers(study: any): Promise<StudyUser[]> {
        return this.studyService.getStudyUserFromStudyId(study.id).then(studyUsers => {
            return studyUsers;
        });
    }

    private fetchHasDUA(study: any): Promise<boolean> {
        return this.studyService.hasDUAByStudyId(study.id).then(hasDUA => {
            return hasDUA;
        });
    }


    goToViewFromEntity(study: any): void {
        Promise.all([
            this.fetchHasDUA(study),
            this.fetchStudyUsers(study),
        ]).then(([hasDUA, studyUsers]) => {
            const studyUser = studyUsers?.find(su => su.userId == this.keycloakService.getUserId());
            if (study.visibleByDefault && study.locked && !this.keycloakService.isUserAdmin()) {
                if (study.accessRequestedByCurrentUser) {
                    this.confirmDialogService.inform('Access request pending', 'You already have asked an access request for this study, wait for the administrator to confirm your access.');
                } else if (!studyUser) {
                    this.confirmDialogService.confirm('Authorization needed',
                        'Before accessing this study you have to request an access to its administrator, do you want to proceed ?'
                    ).then(result => {
                        if (result) {
                            this.router.navigate(['/access-request/study/' + study.id]);
                        }
                    });
                } else if (hasDUA && !studyUser?.confirmed) {
                    const title: string = 'Data User Agreement awaiting for signing';
                    const text: string = 'You are a member of at least one study that needs you to accept its data user agreement. '
                        + 'Until you have agreed those terms you cannot access to any data from these studies. '
                        + 'Would you like to review those terms now?';
                    const buttons = {yes: 'Yes, proceed to the signing page', cancel: 'Later'};
                    this.confirmService.confirm(title, text, buttons).then(response => {
                        if (response == true) this.router.navigate(['/dua']);
                    });
                } else if (studyUser.expired) {
                    const currentExpirationDate: Date = studyUser?.expiration;
                    this.accessRequestService.openAccessExtensionModal(currentExpirationDate, study);
                } else {
                    super.goToViewFromEntity(study);
                }
            }
        });
    }
}
