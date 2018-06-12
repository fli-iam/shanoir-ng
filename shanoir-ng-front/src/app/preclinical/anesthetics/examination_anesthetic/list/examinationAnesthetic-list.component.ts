import {Component, Input, ViewChild, ViewContainerRef} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { ExaminationAnesthetic } from '../shared/examinationAnesthetic.model';
import { ExaminationAnestheticService } from '../shared/examinationAnesthetic.service';
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';

@Component({
  selector: 'examination-anesthetics-list',
  templateUrl:'examinationAnesthetic-list.component.html',
  styleUrls: ['examinationAnesthetic-list.component.css'], 
  providers: [ExaminationAnestheticService]
})
export class ExaminationAnestheticsListComponent {
  @Input() examination_id:number;
    
  public examAnesthetics: ExaminationAnesthetic[];
  public loading: boolean = false;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
    
    constructor(
        public examAnestheticsService: ExaminationAnestheticService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
            this.getExaminationAnesthetics(this.examination_id); 
            this.createColumnDefs();
     }
    
    getExaminationAnesthetics(examination_id:number): void {
        this.loading = true;
        this.examAnestheticsService.getExaminationAnesthetics(examination_id).then(examAnesthetics => {
            if(examAnesthetics){
                this.examAnesthetics = examAnesthetics;
            }else{
                this.examAnesthetics = [];
            }
            this.loading = false;
        }).catch((error) => {
            this.examAnesthetics = [];
        }); 
    }
     
    
    delete(examAnesthetic: ExaminationAnesthetic): void {      
      this.examAnestheticsService.delete(examAnesthetic).then((res) => this.getExaminationAnesthetics(this.examination_id));
    }
    
    viewExamAnesthetic = (examAnesthetic: ExaminationAnesthetic) => {
        this.router.navigate(['/preclinical-examination-edit/', examAnesthetic.id]);
    }
    
    // Grid columns definition
    private createColumnDefs() {
        function dateRenderer(date) {
            if (date) {
                return new Date(date).toLocaleDateString();
            }
            return null;
        };
        function castToString(id: number) {
            return String(id);
        };
        function checkNullValue(value: any) {
            if(value){
                return value;
            }
            return '';
        };
        function checkNullValueReference(reference: any) {
            if(reference){
                return reference.value;
            }
            return '';
        };
        this.columnDefs = [
            /*{headerName: "ID", field: "id", type: "id", cellRenderer: function (params: any) {
                return castToString(params.data.id);
            }},*/
            {headerName: "Anesthetic", field: "anesthetic.name"},
            {headerName: "Dose", field: "dose", type: "dose", cellRenderer: function (params: any) {
                return checkNullValue(params.data.dose);
            }},
            {headerName: "Dose Unit", field: "dose_unit.value", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.dose_unit.value);
            }},
            {headerName: "Injection interval", field: "injectionInterval"},
            {headerName: "Injection site", field: "injectionSite"},
            {headerName: "Injection type", field: "injectionType"},
            {headerName: "Start Date", field: "startDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.startDate);
            }},
            {headerName: "End Date", field: "endDate", type: "date", cellRenderer: function (params: any) {
                return dateRenderer(params.data.endDate);
            }}   
        ];
        
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({ headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteExamAnestheticConfirmDialog },
                {
                    headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target: "/preclinical-examination", getParams: function(item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target: "/preclinical-examination", getParams: function(item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }
        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new anesthetic", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical-examination", getParams: function(item: any): Object {
                    return { mode: "create" };
                }
            });
            this.customActionDefs.push({ title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/preclinical-examination", getParams: function(item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
        }
    }
    
    openDeleteExamAnestheticConfirmDialog = (item: ExaminationAnesthetic) => {
         this.confirmDialogService
                .confirm('Delete subject', 'Are you sure you want to delete examination anesthetic ' + item.id + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let examAnesthetic of this.examAnesthetics) {
            if (examAnesthetic["isSelectedInTable"]) ids.push(examAnesthetic.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}