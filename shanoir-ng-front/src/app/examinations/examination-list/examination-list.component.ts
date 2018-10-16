import { Component, ViewContainerRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { Page, Pageable } from '../../shared/components/table/pageable.model';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Examination } from '../shared/examination.model';
import { ExaminationService } from '../shared/examination.service';
import { TableComponent } from '../../shared/components/table/table.component';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

@Component({
    selector: 'examination-list',
    templateUrl: 'examination-list.component.html',
    styleUrls: ['examination-list.component.css'],
})
export class ExaminationListComponent {
    private examinations: Examination[];
    private columnDefs: any[];
    private customActionDefs: any[];
    private createAcqEquip = false;
    private nbExaminations: number = 0;
    @ViewChild('examTable') examTable: TableComponent;

    constructor(
            private examinationService: ExaminationService, 
            private confirmDialogService: ConfirmDialogService,
            private viewContainerRef: ViewContainerRef, 
            private keycloakService: KeycloakService,
            private msgService: MsgBoxService,
            private router: Router) {
        this.createColumnDefs();
    }

    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.examinationService.getPage(pageable).then(page => {
            return page;
        });
    }

    // Grid columns definition
    private createColumnDefs() {
        function dateRenderer(date: number) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };

        this.columnDefs = [
            { headerName: "Examination id", field: "id" },
            {
                headerName: "Subject", field: "subject.name", cellRenderer: function (params: any) {
                    return (params.data.subject) ? params.data.subject.name : "";
                }
            },
            {
                headerName: "Examination date", field: "examinationDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.examinationDate);
                }, width: "100px"
            },
            {
                headerName: "Research study", field: "studyName", type: "link", 
                action: (examination: Examination) => this.router.navigate(['/study/details/' + examination.studyId])
            },
            { headerName: "Examination executive", field: "" },
            {
                headerName: "Center", field: "centerName", type: "link", 
                action: (examination: Examination) => this.router.navigate(['/center/details/' + examination.centerId])
            }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: (item: any) => this.router.navigate(['/examination/edit/' + item.id])
            });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: (item: any) => this.router.navigate(['/examination/details/' + item.id])
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new examination.", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/examination/create"
            });
        }
    }

    private onRowClick(exam: Examination) {
        if (!this.keycloakService.isUserGuest()) {
            this.router.navigate(['/examination/details/' + exam.id])
        }
    }

    openDeleteExaminationConfirmDialog(item: Examination) {
        this.confirmDialogService
                .confirm('Delete examination', 'Are you sure you want to delete examination ' + item.id + '?',
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.examinationService.delete(item.id).then(() => {
                            this.examTable.refresh();
                            this.msgService.log('info', 'The examination has been sucessfully deleted');
                        });
                    }
                });
    }

    deleteAll = () => {
        let ids: number[] = [];
        for (let examination of this.examinations) {
            if (examination["isSelectedInTable"]) ids.push(examination.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }
}