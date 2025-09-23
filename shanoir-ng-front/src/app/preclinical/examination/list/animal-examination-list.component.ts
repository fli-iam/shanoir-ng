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

import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationAnestheticService } from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { ExtraDataService } from '../../extraData/extraData/shared/extradata.service';
import {  Page, Pageable } from '../../../shared/components/table/pageable.model';
import { TableComponent } from '../../../shared/components/table/table.component';
import { ColumnDefinition } from '../../../shared/components/table/column.definition.type';
import { EntityListComponent } from '../../../shared/components/entity/entity-list.component.abstract';
import { ShanoirError } from '../../../shared/models/error.model';
import { ExaminationService } from '../../../examinations/shared/examination.service';


@Component({
    selector: 'animal-examination-list',
    templateUrl: 'animal-examination-list.component.html',
    standalone: false
})
export class AnimalExaminationListComponent extends EntityListComponent<Examination>{
    @ViewChild('examTable', { static: false }) table: TableComponent;

    constructor(
        private examinationService:ExaminationService,
        private examAnestheticsService: ExaminationAnestheticService,
    	private extradataService: ExtraDataService)

    {
            super('preclinical-examination');
            this.manageDelete();
    }

    getService(): EntityService<Examination> {
        return this.examinationService;
    }

    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.examinationService.getPage(pageable, true, "", "");
    }

    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
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

    protected openDeleteConfirmDialog = (entity: Examination) => {
        if (!this.keycloakService.isUserAdminOrExpert()) return;
        this.getSelectedExamination(entity.id).then(selectedExamination => {
        this.confirmDialogService
            .confirm(
                'Delete', 'Are you sure you want to delete preclinical-examination n° ' + entity.id + ' ?'
            ).then(res => {
                if (res) {
                    this.getService().delete(selectedExamination.id).then(() => {
                        this.onDelete.next({entity: selectedExamination});
                        this.table.refresh();
                        this.consoleService.log('info', 'The preclinical-examination n°' + entity.id + ' was sucessfully deleted');
                    }).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next({entity: selectedExamination, error: new ShanoirError(reason)});
                            if (reason.error.code != 422) throw Error(reason);
                        }
                    });
                }
            })

        });
    }

    private getSelectedExamination(id : number): Promise<Examination>{
        return this.examinationService.get(id).then(examination => {
            return examination;
        });
    }

    private manageDelete() {
        this.subscriptions.push(
            this.onDelete.subscribe(response => {
                this.deleteExamination(response.entity.id)
            })
        );
    }

    getOptions() {
        return {
            new: false,
            view: true,
            edit: false,
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    deleteExamination(examinationId: number) {
        // Delete examination and refresh page
        this.examAnestheticsService.getExaminationAnesthetics(examinationId)
               .then(examAnesthetics => {
               if (examAnesthetics && examAnesthetics.length > 0) {
                   //Should be only one
                    let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                    this.examAnestheticsService.deleteAnesthetic(examAnesthetic);
               }
        });
        this.extradataService.getExtraDatas(examinationId).then(extradatas => {
            if(extradatas && extradatas.length > 0){
            	for (let data of extradatas) {
            		this.extradataService.deleteExtradata(data).then((res) => {});
            	}
            }
        });

    }



}
