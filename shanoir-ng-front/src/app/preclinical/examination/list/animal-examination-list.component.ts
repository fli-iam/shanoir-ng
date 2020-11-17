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
import {Component, Input, ViewChild, ViewContainerRef, OnChanges} from '@angular/core'

import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { Examination } from '../../../examinations/shared/examination.model';
import { ExaminationAnestheticService } from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { ExtraDataService } from '../../extraData/extraData/shared/extradata.service';

import {  Page, Pageable } from '../../../shared/components/table/pageable.model';
import { TableComponent } from '../../../shared/components/table/table.component';
import { EntityListComponent } from '../../../shared/components/entity/entity-list.component.abstract';
import { ShanoirError } from '../../../shared/models/error.model';
import { ExaminationService } from '../../../examinations/shared/examination.service';


@Component({
    selector: 'animal-examination-list',
    templateUrl: 'animal-examination-list.component.html', 
    providers: [ExaminationService]
})
export class AnimalExaminationListComponent extends EntityListComponent<Examination>{
    @ViewChild('examTable') table: TableComponent;
    
    constructor(
        private examinationService:ExaminationService, 
        private examAnestheticsService: ExaminationAnestheticService,
    	private extradataService: ExtraDataService)
    	
    {
            super('preclinical-examination');
            this.manageDelete();
     }
    
    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.examinationService.getPage(pageable, true);
    }

    getColumnDefs(): any[] {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        let colDef: any[] = [
            { headerName: "Examination id", field: "id" },
            {headerName: "Subject", field: "subjectId", cellRenderer: (params: any) => (params.data.subject) ? params.data.subject.name : ""},
            {
                headerName: "Examination date", field: "examinationDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.examinationDate);
                }, width: "100px"
            },
            {
                headerName: "Research study", field: "studyId", type: "link",
                action: (examination: Examination) => this.router.navigate(['/study/details/' + examination.study.id]),
                cellRenderer: (params: any) => (params.data.study) ? params.data.study.name : ""
            },
            { headerName: "Examination executive", field: "" },
            {
                headerName: "Center", field: "centerId", type: "link",
                action: (examination: Examination) => this.router.navigate(['/center/details/' + examination.center.id]),
                cellRenderer: (params: any) => (params.data.center) ? params.data.center.name : ""
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
                    selectedExamination.delete().then(() => {
                        this.onDelete.next(selectedExamination);
                        this.table.refresh();
                        this.msgBoxService.log('info', 'The preclinical-examination sucessfully deleted');
                    }).catch(reason => {
                        if (reason && reason.error) {
                            this.onDelete.next(new ShanoirError(reason));
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
        this.subscribtions.push(
            this.onDelete.subscribe(response => {
                this.deleteExamination(response.id)
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