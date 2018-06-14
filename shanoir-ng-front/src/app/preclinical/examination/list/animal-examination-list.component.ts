import {Component, Input, ViewChild, ViewContainerRef, OnChanges} from '@angular/core'
import { Router } from '@angular/router';

import { ConfirmDialogService } from "../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";
import { AnimalExaminationService } from '../shared/animal-examination.service';
import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { Examination } from '../../../examinations/shared/examination.model';
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';
import { Pageable } from '../../../shared/components/table/pageable.model';

@Component({
    selector: 'animal-examination-list',
    templateUrl: 'animal-examination-list.component.html'
})
export class AnimalExaminationListComponent {

    public examinations: Examination[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public rowClickAction: Object;
    public loading: boolean = false;
    private createAcqEquip = false;
    private pageable: Pageable;
    public nbExaminations: number = 0;

    constructor(private annimalExaminationService: AnimalExaminationService, private confirmDialogService: ConfirmDialogService,
        private viewContainerRef: ViewContainerRef, private keycloakService: KeycloakService) {
        this.countExaminations();
        this.getExaminations();
        this.createColumnDefs();
    }

    // Grid data
    getExaminations(): void {
        this.loading = true;
        this.examinations = [];
        this.annimalExaminationService.getExaminations(this.pageable).then(examinations => {
            if (examinations) {
            	this.examinations = this.filterPreclinicalExaminations(examinations);
            }
            this.loading = false;
        })
            .catch((error) => {
                // TODO: display error
                this.examinations = [];
            });
    }
    
    filterPreclinicalExaminations(examinations: Examination[]): Examination[]{
    	let preclinicalEx: Examination[] = [];
    	for (let ex of examinations) {
    		if (ex.preclinical == true){
    			preclinicalEx.push(ex);
    		}
    	}
    	return preclinicalEx;
    }

    countExaminations(): void {
        this.loading = true;
        this.annimalExaminationService.countExaminations().then(nbExaminations => {
            this.nbExaminations = nbExaminations;
        })
            .catch((error) => {
                // TODO: display error
                this.examinations = [];
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
                headerName: "Research study", field: "studyName", type: "link", clickAction: {
                    target: "/study", getParams: function (examination: Examination): Object {
                        return { id: examination.studyId, mode: "view" };
                    }
                }
            },
            { headerName: "Examination executive", field: "" },
            {
                headerName: "Center", field: "centerName", type: "link", clickAction: {
                    target: "/center", getParams: function (examination: Examination): Object {
                        return { id: examination.centerId, mode: "view" };
                    }
                }
            }
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push(
            	{headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteExaminationConfirmDialog},
                {
                    headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target: "/preclinical-examination", getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target: "/preclinical-examination", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new examination.", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical-examination", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/examination", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
        }
    }

    openDeleteExaminationConfirmDialog = (item: Examination) => {
        this.confirmDialogService
            .confirm('Delete examination', 'Are you sure you want to delete the following entity?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteExamination(item.id);
                }
            })
    }

    deleteExamination(examinationId: number) {
        // Delete examination and refresh page
        this.annimalExaminationService.delete(examinationId).then((res) => this.getExaminations());
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

    public reloadExaminations(pageable: Pageable): void {
        this.pageable = pageable;
        this.getExaminations();
    }

}