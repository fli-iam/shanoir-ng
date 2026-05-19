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
import { CopyData, CopyDataService } from 'src/app/studies/shared/copy-data.service';

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

    constructor(private http: HttpClient,
        private studyRightsService: StudyRightsService,
        private studyService: StudyService,
        private keycloakService: KeycloakService,
        private copyDataService: CopyDataService) {
    }

    public setUp(inputDatasets: InputDataset[], modalRef: any) {
        this.fetchStudies().then(() => {
            this.sortStudies();
            this.checkImportRightsOnDatasets();
        });
        this.ownRef = modalRef;
        this.inputDatasets = inputDatasets;
    }

    private fetchStudies(): Promise<void> {
        return this.studyService.findStudyIdNamesCanImport().then(studies => {
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

    private checkImportRightsOnDatasets() {
        const hasEveryForAll: boolean = this.inputDatasets.every(ds => {
            return this.studies.find(s => s.id === ds.studyId);
        });
        if (!hasEveryForAll) {
            this.statusMessage = "You must have IMPORT right on all the studies of the selected datasets to proceed with the copy.";
            this.canCopy = false;
        }
    }

    public copy() {
        this.canCopy = false;
        this.checkRightsOnSelectedStudies(this.selectedStudy.id).then(() => {
            this.isDatasetInStudy = this.checkDatasetBelongToStudy(this.inputDatasets, this.selectedStudy.id);

            if (!this.hasRight) {
                this.statusMessage = 'Missing rights for study ' + this.selectedStudy.name + ' please make sure you have IMPORT right.';
            } else if (this.isDatasetInStudy) {
                this.statusMessage = 'Selected dataset(s) already belong to selected study.';
            } else {
                return this.copyDataService.copy(this.buildCopyData()).then(() => {
                    this.close();
                }).catch(reason => {
                    this.canCopy = false;
                    if (reason.status == 403) {
                        this.statusMessage = "You must have IMPORT right.";
                    } else throw Error(reason);
                });
            }
        });
    }

    private buildCopyData(): CopyData {
        console.log("Building copy data with input datasets: ", this.inputDatasets);
        return {
            datasets: this.inputDatasets.map(d => ({
                datasetId: d.datasetId,
                centerId: d.centerId,
                subjectId: d.subjectId
            })),
            targetStudyId: this.selectedStudy.id,
            subjects: this.subjectIds
                .map(s => ({
                    id: s,
                    newName: this.subjectIds.length == 1 ? this.subjectName : null
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
                return (rights.includes(StudyUserRight.CAN_IMPORT));
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
