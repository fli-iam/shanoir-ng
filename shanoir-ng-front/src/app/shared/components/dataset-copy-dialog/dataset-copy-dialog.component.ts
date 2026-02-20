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
import { Component } from '@angular/core';

import { StudyService } from 'src/app/studies/shared/study.service';
import { CopyDataService } from 'src/app/studies/shared/copy-data.service';
import { CopyData } from 'src/app/studies/study/copy-csv.component';

import { StudyRightsService } from "../../../studies/shared/study-rights.service";
import { StudyUserRight } from "../../../studies/shared/study-user-right.enum";
import { ServiceLocator } from "../../../utils/locator.service";
import { ConsoleService } from "../../console/console.service";
import { KeycloakService } from "../../keycloak/keycloak.service";
import { IdName } from '../../models/id-name.model';


export type InputDataset = {
    datasetId: number,
    centerId: number,
    subjectId: number,
    studyId: number
};

@Component({
    selector: 'user-action-dialog',
    templateUrl: 'dataset-copy-dialog.component.html',
    styleUrls: ['dataset-copy-dialog.component.css'],
    standalone: false
})
export class DatasetCopyDialogComponent {

    protected studies: IdName[];
    protected inputDatasets: InputDataset[] = [];
    protected ownRef: any;
    protected selectedStudy: IdName;
    protected subjectName: string = '';
    protected statusMessage: string;
    protected hasRight: boolean = false;
    protected isDatasetInStudy: boolean = false;
    protected canCopy: boolean = true;
    protected centerIds: number[] = [];
    protected subjectIds: number[] = [];
    protected consoleService = ServiceLocator.injector.get(ConsoleService);
    protected readonly BATCH_SIZE: number = 1000;

    constructor(private http: HttpClient,
        private studyRightsService: StudyRightsService,
        private studyService: StudyService,
        private keycloakService: KeycloakService,
        private copyDataService: CopyDataService) {
    }

    public setUp(inputDatasets: InputDataset[], modalRef: any) {
        this.fetchStudies().then(() => {
            this.sortStudies();
            this.checkAdminRightsOnDatasets();
        });
        this.ownRef = modalRef;
        this.inputDatasets = inputDatasets;
    }

    private fetchStudies(): Promise<void> {
        return this.studyService.findStudyIdNamesIcanAdmin().then(studies => {
            this.studies = studies;
        });
    }

    private sortStudies() {
        this.studies.sort((a: any, b: any) => { return a.name.localeCompare(b.name, undefined, { sensitivity: 'base' }) });
        const ids: number[] = [];
        for (const line of this.inputDatasets) {
            if (!this.centerIds.includes(line.centerId)) {
                this.centerIds.push(line.centerId);
            }
            if (line.subjectId != null) {
                if (!this.subjectIds.includes(line.subjectId)) {
                    this.subjectIds.push(line.subjectId);
                }
            } else {
                ids.push(line.datasetId);
                this.statusMessage = "Some of the selected datasets (id = " + ids.join(", ") + ") have no subject, can't proceed with the copy.";
                this.canCopy = false;
            }
        }
    }

    private checkAdminRightsOnDatasets() {
        const hasEveryForAll: boolean = this.inputDatasets.every(ds => {
            return this.studies.find(s => s.id === ds.studyId);
        });
        if (!hasEveryForAll) {
            this.statusMessage = "You must have ADMIN right on all the studies of the selected datasets to proceed with the copy.";
            this.canCopy = false;
        }
    }

    public copy() {
        this.canCopy = false;
        this.checkRightsOnSelectedStudies(this.selectedStudy.id).then(() => {
            this.isDatasetInStudy = this.checkDatasetBelongToStudy(this.inputDatasets, this.selectedStudy.id);

            if (!this.hasRight) {
                this.statusMessage = 'Missing rights for study ' + this.selectedStudy.name + ' please make sure you have ADMIN right.';
            } else if (this.isDatasetInStudy) {
                this.statusMessage = 'Selected dataset(s) already belong to selected study.';
            } else {
                return this.doCopy().then(() => {
                    this.close();
                }).catch(reason => {
                    this.canCopy = true;
                    if (reason.status == 403) {
                        this.statusMessage = "You must be admin or expert.";
                    } else throw Error(reason);
                });
            }
        });
    }

    private doCopy(): Promise<void> {
        const nbPages: number = Math.ceil(this.inputDatasets.length / this.BATCH_SIZE);
        const copyData: CopyData[] = [];
        for (let page = 1; page <= nbPages; page++) {
            copyData.push(this.buildCopyData(page, this.BATCH_SIZE));
        }
        let promise: Promise<void> = Promise.resolve();
        if (copyData.length > 1) {
            this.consoleService.log('info', 'The copy of ' + this.inputDatasets.length + ' datasets towards study ' 
                + this.selectedStudy.name + ' has started in batch mode, it may take a while. '
                + copyData.length + ' batch(es) will be processed sequentially.');
        } else {
            this.consoleService.log('info', 'The copy of ' + this.inputDatasets.length + ' datasets towards study ' 
                + this.selectedStudy.name + ' has started.');
        }
        copyData.forEach(cd => {
            promise = promise.then(() => {
                return this.copyDataService.copyData(cd);
            });
        });
        return promise;
    }

    private buildCopyData(page: number, pageSize: number): CopyData {
        const start = (page - 1) * pageSize;
        const end = start + pageSize;
        const datasetSlice = this.inputDatasets.slice(start, end);
        const centerIdSet = new Set(datasetSlice.map(d => d.centerId));
        const subjectIdSet = new Set(datasetSlice.map(d => d.subjectId));
        return {
            datasetIds: datasetSlice.map(d => d.datasetId),
            targetStudyId: this.selectedStudy.id,
            centerIds: this.centerIds.filter(c => centerIdSet.has(c)),
            subjects: this.subjectIds
                .filter(s => subjectIdSet.has(s))
                .map(s => ({
                    id: s,
                    newName: null
                }))
        };
    }

    public checkDatasetBelongToStudy(lines: InputDataset[], studyId: number) {
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

    pickStudy(study: IdName) {
        this.selectedStudy = study;
    }

    close() {
        this.ownRef.destroy();
    }

}
