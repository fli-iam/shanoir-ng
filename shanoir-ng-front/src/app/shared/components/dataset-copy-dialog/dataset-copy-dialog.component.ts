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

import { HttpClient } from '@angular/common/http';
import {Component} from '@angular/core';
import {Study} from "../../../studies/shared/study.model";
import * as AppUtils from "../../../utils/app.utils";
import {KeycloakService} from "../../keycloak/keycloak.service";
import {StudyUserRight} from "../../../studies/shared/study-user-right.enum";
import {StudyRightsService} from "../../../studies/shared/study-rights.service";
import {ServiceLocator} from "../../../utils/locator.service";
import {ConsoleService} from "../../console/console.service";
import {StudyService} from "../../../studies/shared/study.service";
import {SolrDocument} from "../../../solr/solr.document.model";

@Component({
    selector: 'user-action-dialog',
    templateUrl: 'dataset-copy-dialog.component.html',
    styleUrls: ['dataset-copy-dialog.component.css'],
    standalone: false
})
export class DatasetCopyDialogComponent {
    title: string;
    message: string;
    studies: Study[];
    selectedStudy: Study;
    datasetsIds: number[];
    statusMessage: string;
    ownRef: any;
    hasRight: boolean = false;
    isDatasetInStudy: boolean = false;
    canCopy: boolean;
    centerIds: string[]=[];
    subjectIds: string[]=[];
    lines: SolrDocument[];
    subjectIdStudyId: string[]=[];
    protected consoleService = ServiceLocator.injector.get(ConsoleService);
    constructor(private http: HttpClient,
                private studyRightsService: StudyRightsService,
                private studyService: StudyService,
                private keycloakService: KeycloakService) {
    }

    ngOnInit() {
        // sort studies by alphabetical order
        this.studies.sort((a: any, b: any) => { return a.name.localeCompare(b.name, undefined, {sensitivity: 'base'})});
        for (let line of this.lines) {
            if (!this.centerIds.includes(line.centerId)) {
                this.centerIds.push(line.centerId);
            }
            if (!this.subjectIds.includes(line.subjectId) && line.subjectId != null) {
                this.subjectIds.push(line.subjectId);
            } else if (line.subjectId == null) {
                this.statusMessage = "Be careful, some of the selected datasets have a null subject.";
                this.canCopy = false;
            }
            if (!this.subjectIdStudyId.includes(line.subjectId + "/" + line.studyId)) {
                this.subjectIdStudyId.push(line.subjectId + "/" + line.studyId);
            }
        }
    }
    public copy() {
        this.canCopy = false;
        this.checkRightsOnSelectedStudies(this.selectedStudy.id).then( () => {
            this.isDatasetInStudy = this.checkDatasetBelongToStudy(this.lines, this.selectedStudy.id);

            if (!this.hasRight) {
                this.statusMessage = 'Missing rights for study ' + this.selectedStudy.name + ' please make sure you have ADMIN right.';
            } else if (this.isDatasetInStudy) {
                this.statusMessage = 'Selected dataset(s) already belong to selected study.';
            } else {
                const formData: FormData = new FormData();
                formData.set('datasetIds', Array.from(this.datasetsIds).join(","));
                formData.set('studyId', this.selectedStudy.id.toString());
                formData.set('centerIds', Array.from(this.centerIds).join(","));
                formData.set('subjectIdStudyId', Array.from(this.subjectIdStudyId).join(","));
                return this.http.post<string>(AppUtils.BACKEND_API_STUDY_URL + '/copyDatasets', formData, { responseType: 'text' as 'json'})
                    .toPromise()
                    .then( () => {
                        this.close();
                        this.consoleService.log('info', 'The copy of ' + this.datasetsIds.length + ' datasets towards study ' + this.selectedStudy.name + ' has started.');
                    }).catch(reason => {
                        this.canCopy = true;
                        if (reason.status == 403) {
                            this.statusMessage = "You must be admin or expert.";
                        } else throw Error(reason);
                    });
            }
        });
    }

    public checkDatasetBelongToStudy(lines: SolrDocument[], studyId: number) {
        return lines.some((line) => {
            return (studyId == Number(line.studyId));
        });
    }

    private async checkRightsOnSelectedStudies(id: number): Promise<void> {
        await this.hasRightsOnStudyId(id).then(res => {
            this.hasRight = res;
        });
    }

    private async hasRightsOnStudyId(studyId: number): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) {
            return Promise.resolve(true);
        } else {
            return this.studyRightsService.getMyRightsForStudy(studyId).then(rights => {
                return (rights.includes(StudyUserRight.CAN_ADMINISTRATE));
            });
        }
    }

    pickStudy(study: Study) {
        this.selectedStudy = study;
    }

    close() {
        this.ownRef.destroy();
    }
}
