import {Component, Input, ViewChild, ViewContainerRef, OnChanges} from '@angular/core'
import { Router } from '@angular/router';

import { ConfirmDialogService } from "../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";
import { AnimalExaminationService } from '../shared/animal-examination.service';
import { ExaminationAnesthetic }    from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.model';
import { Examination } from '../../../examinations/shared/examination.model';
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';
import { ExaminationAnestheticService } from '../../anesthetics/examination_anesthetic/shared/examinationAnesthetic.service';
import { ExaminationExtraDataService } from '../../extraData/extraData/shared/extradata.service';

import { FilterablePageable, Page, Pageable } from '../../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../../shared/components/table/browser-paging.model';
import { TableComponent } from '../../../shared/components/table/table.component';

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
   @ViewChild('examTable') table: TableComponent;

    constructor(
    	private animalExaminationService: AnimalExaminationService, 
    	private confirmDialogService: ConfirmDialogService,
    	private examAnestheticsService: ExaminationAnestheticService,
    	private extradataService: ExaminationExtraDataService,
    	private viewContainerRef: ViewContainerRef, 
        private keycloakService: KeycloakService) {
        	this.createColumnDefs();
    }
    
    
    getPage(pageable: Pageable): Promise<Page<Examination>> {
        return this.animalExaminationService.getPage(pageable).then(page => {
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
        this.examAnestheticsService.getExaminationAnesthetics(examinationId)
               .then(examAnesthetics => {
               if (examAnesthetics && examAnesthetics.length > 0) {
               	//Should be only one
                let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                this.examAnestheticsService.delete(examAnesthetic).then((res) => {
                	
                });
               }
        });
        this.extradataService.getExtraDatas(examinationId).then(extradatas => {
            if(extradatas && extradatas.length > 0){
            	for (let data of extradatas) {
            		this.extradataService.delete(data).then((res) => {});
            	}
            }
        });
       
        this.animalExaminationService.delete(examinationId);
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