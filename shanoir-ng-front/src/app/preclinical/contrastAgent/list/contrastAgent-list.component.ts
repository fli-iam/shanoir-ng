import {Component, Input, ViewChild, ViewContainerRef, OnChanges} from '@angular/core'
import { Router } from '@angular/router'; 

import { ConfirmDialogService } from "../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";

import { ContrastAgent } from '../shared/contrastAgent.model';
import { ContrastAgentService } from '../shared/contrastAgent.service';
import { InjectionInterval } from "../../shared/enum/injectionInterval";
import { InjectionSite } from "../../shared/enum/injectionSite";
import { InjectionType } from "../../shared/enum/injectionType";
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';
import { FilterablePageable, Page } from '../../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../../shared/components/table/browser-paging.model';
import { TableComponent } from '../../../shared/components/table/table.component';

@Component({
  selector: 'contrast-agent-list',
  templateUrl:'contrastAgent-list.component.html',
  styleUrls: ['contrastAgent-list.component.css'], 
  providers: [ContrastAgentService]
})
export class ContrastAgentsListComponent {
  @Input() protocol_id:number;
  public agents: ContrastAgent[] = [];
  private agentsPromise: Promise<void> = this.getContrastAgents();
  private browserPaging: BrowserPaging<ContrastAgent>;
  public rowClickAction: Object;
  public columnDefs: any[];
  public customActionDefs: any[];
  @ViewChild('contrastAgentTable') table: TableComponent;
  
    
    constructor(
        public contrastAgentsService: ContrastAgentService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
            this.createColumnDefs();
     }
    
    ngOnChanges(){
        if(this.protocol_id){
          this.getContrastAgents();
          //this.createColumnDefs();
        }
    }
    
    getPage(pageable: FilterablePageable): Promise<Page<ContrastAgent>> {
        return new Promise((resolve) => {
            this.agentsPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }
    
    
    
    getContrastAgents(): Promise<void> {
    	this.agents = [];
    	this.browserPaging = new BrowserPaging(this.agents, this.columnDefs);
        return this.contrastAgentsService.getContrastAgents(this.protocol_id).then(agents => {
            if(agents){
                this.agents = agents;
            }else{
                this.agents = [];
            }
            this.browserPaging.setItems(this.agents);
            this.browserPaging.setColumnDefs(this.columnDefs);
            this.table.refresh();
        }) 
    }
    
    
    delete(agent:ContrastAgent): void {      
      this.contrastAgentsService.delete(this.protocol_id,agent.id).then((res) => this.getContrastAgents());
    }
    
    
    // Grid columns definition
    private createColumnDefs() {
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
            {headerName: "Name", field: "name", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.name);
            }},
            {headerName: "Manufactured Name", field: "manufactured_name", type: "string", cellRenderer: function (params: any) {
                return checkNullValue(params.data.manufactured_name);
            }},
            {
                headerName: "Concentration", field: "concentration", type: "number", cellRenderer: function(params: any) {
                    return checkNullValue(params.data.concentration);
                }
            },
            {headerName: "Concentration Unit", field: "concentration_unit", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.concentration_unit);
            }},
            {headerName: "Dose", field: "dose", type: "dose", cellRenderer: function (params: any) {
                return checkNullValue(params.data.dose);
            }},
            {headerName: "Dose Unit", field: "dose_unit", type: "reference", cellRenderer: function (params: any) {
                return checkNullValueReference(params.data.dose_unit);
            }},
            {headerName: "Injection interval", field: "injection_interval", type: "string", cellRenderer: function (params: any) {
                return InjectionInterval[params.data.injection_interval];
            }},
            {headerName: "Injection site", field: "injection_site", type: "string", cellRenderer: function (params: any) {
                return InjectionSite[params.data.injection_site];
            }},
            {headerName: "Injection type", field: "injection_type", type: "string", cellRenderer: function (params: any) {
                return InjectionType[params.data.injection_type];
            }}
        ];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteContrastAgentConfirmDialog},
            {headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target : "/preclinical-contrastagent", getParams: function(item: any): Object {
                return {id: item.id, mode: "edit"};
            }});
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target : "/preclinical-contrastagent", getParams: function(item: any): Object {
                return {id: item.id, mode: "view"};
            }});
        }
        
       this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new contrast agent", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/preclinical-contrastagent", getParams: function(item: any): Object {
                    return { mode: "create" };
                }
            });
            this.customActionDefs.push({ title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });
        }
        
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {target : "/preclinical-contrastagent", getParams: function(item: any): Object {
                    return {id: item.id, mode: "view"};
            }};
        }
    }
    
    private onRowClick(item: ContrastAgent) {
        if (!this.keycloakService.isUserGuest()) {
            this.router.navigate(['/preclinical-contrastagent'], { queryParams: { id: item.id, mode: "view" } });
        }
    }
    
    openDeleteContrastAgentConfirmDialog = (item: ContrastAgent) => {
         this.confirmDialogService
                .confirm('Delete contrast agent', 'Are you sure you want to delete contrast agent ' + item.id + '?', 
                    this.viewContainerRef)
                .subscribe(res => {
                    if (res) {
                        this.delete(item);
                    }
                });
    }
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let agent of this.agents) {
            if (agent["isSelectedInTable"]) ids.push(agent.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}