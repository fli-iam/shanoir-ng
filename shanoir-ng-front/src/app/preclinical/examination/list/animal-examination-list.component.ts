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
import {Component, ViewChild} from '@angular/core'

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationAnestheticService } from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { ExtraDataService } from '../../extraData/extraData/shared/extradata.service';
import { Page, Pageable } from '../../../shared/components/table/pageable.model';
import { TableComponent } from '../../../shared/components/table/table.component';
import { ColumnDefinition } from '../../../shared/components/table/column.definition.type';
import { EntityListComponent } from '../../../shared/components/entity/entity-list.component.abstract';
import { ExaminationService } from '../../../examinations/shared/examination.service';
import { StudyService } from '../../../studies/shared/study.service';
import { StudyUserRight } from '../../../studies/shared/study-user-right.enum';


@Component({
    selector: 'animal-examination-list',
    templateUrl: 'animal-examination-list.component.html',
    standalone: false
})
export class AnimalExaminationListComponent extends EntityListComponent<Examination>{
    @ViewChild('table', { static: false }) table: TableComponent;

    private studiesICanAdmin: number[];
    private studyIdsForCurrentUser: number[];

    constructor(
        private examinationService:ExaminationService,
        private examAnestheticsService: ExaminationAnestheticService,
    	private extradataService: ExtraDataService,
    	private studyService: StudyService
    ) {
        super('preclinical-examination');
        this.studyService.findStudyIdsIcanAdmin().then(ids => this.studiesICanAdmin = ids);
        this.studyService.getStudiesByRight(StudyUserRight.CAN_IMPORT).then( studies => this.studyIdsForCurrentUser = studies);
        this.subscriptions.push(this.onDelete.subscribe(response => this.deleteExaminationRelatedData(response.entity.id)));
    }

    getService(): EntityService<Examination> {
        return this.examinationService;
    }

    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.examinationService.getPage(pageable, true, this.table.filter.searchStr? this.table.filter.searchStr : "", this.table.filter.searchField ? this.table.filter.searchField : "").then(page => {
            return page;
        });
    }

    getColumnDefs(): ColumnDefinition[] {
        const colDef: ColumnDefinition[] = [
            { headerName: "Id", field: "id" },
            {
                headerName: "Subject", field: "subject.name", cellRenderer: (params: any) => (params.data.subject) ? params.data.subject.name : "",
                route: (examination: Examination) => examination.subject ? '/preclinical-subject/details/' + examination.subject.id : null
            },
            {
                headerName: "Examination date", field: "examinationDate", type: "date", width: "100px"
            },
            {
                headerName: "Study", field: "study.name",
                route: (examination: Examination) => examination.study ? '/study/details/' + examination.study.id : null
            },
            {
                headerName: "Acquisition Center", field: "center.name",
                route: (examination: Examination) => examination.center ? '/center/details/' + examination.center.id : null
            }
        ];
        return colDef;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    private getSelectedExamination(id : number): Promise<Examination>{
        return this.examinationService.get(id).then(examination => {
            return examination;
        });
    }

    getOptions() {
        return {
            new: this.keycloakService.isUserAdminOrExpert(),
            view: true,
            edit: this.keycloakService.isUserAdminOrExpert(),
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    canEdit(ex: Examination): boolean {
        return this.keycloakService.isUserAdmin() || this.studyIdsForCurrentUser.includes(ex.study.id);
    }

    canDelete(exam: Examination): boolean {
        return this.keycloakService.isUserAdmin() || (
            exam.study
            && this.studiesICanAdmin
            && this.studiesICanAdmin.includes(exam.study.id)
        );
    }

    async deleteExaminationRelatedData(examinationId: number): Promise<void> {
         // Delete anesthetic (0 or 1)
        const examAnesthetics = await this.examAnestheticsService.getExaminationAnesthetics(examinationId);
        if (examAnesthetics?.length)
            await this.examAnestheticsService.deleteAnesthetic(examAnesthetics[0]);

        // Delete all extra data (0 or multiple)
        const extraData = await this.extradataService.getExtraDatas(examinationId);
        if (extraData?.length)
            await Promise.all(extraData.map(data => this.extradataService.deleteExtradata(data)));
    }
}
