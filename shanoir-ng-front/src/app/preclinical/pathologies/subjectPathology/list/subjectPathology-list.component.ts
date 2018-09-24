import {Component, OnInit, Input, Output, EventEmitter, ViewChild, ViewContainerRef, OnChanges, ChangeDetectionStrategy} from '@angular/core'
import { Router } from '@angular/router';

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { SubjectPathology } from '../shared/subjectPathology.model';
import { SubjectPathologyService } from '../shared/subjectPathology.service';

import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';
import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';
import { FilterablePageable, Page } from '../../../../shared/components/table/pageable.model';
import { BrowserPaging } from '../../../../shared/components/table/browser-paging.model';
import { TableComponent } from '../../../../shared/components/table/table.component';


@Component({
    selector: 'subject-pathology-list',
    templateUrl: 'subjectPathology-list.component.html',
    styleUrls: ['subjectPathology-list.component.css'],
    providers: [SubjectPathologyService]
})
@ModesAware
export class SubjectPathologiesListComponent implements OnChanges {
    public pathologies: SubjectPathology[] = [];
    private subjectPathologiesPromise: Promise<void> = this.getSubjectPathologies();
    private browserPaging: BrowserPaging<SubjectPathology>;
    public rowClickAction: Object;
    public columnDefs: any[];
    public customActionDefs: any[];
    @Input() mode: Mode = new Mode();
    @Input() canModify: Boolean = false;
    @Input() preclinicalSubject: PreclinicalSubject;
    public toggleFormSP: boolean = false;
    public createSPMode: boolean = false;
    public pathoSelected: SubjectPathology;
    //onSelected = new EventEmitter<SubjectPathology>()  ; 
    
    @ViewChild('subjectPathologiesTable') table: TableComponent;
    
    constructor(
        public subjectPathologyService: SubjectPathologyService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
            this.createColumnDefs();
    }

	getPage(pageable: FilterablePageable): Promise<Page<SubjectPathology>> {
        return new Promise((resolve) => {
        	if (this.subjectPathologiesPromise){
            	this.subjectPathologiesPromise.then(() => {
                	resolve(this.browserPaging.getPage(pageable));
            	});
            }
        });
    }
    
    getSubjectPathologies(): Promise<void> {
        this.pathologies = [];
        this.browserPaging = new BrowserPaging(this.pathologies, this.columnDefs);
    	if (this.preclinicalSubject && this.preclinicalSubject.animalSubject && this.preclinicalSubject.animalSubject.id) {
            return this.subjectPathologyService.getSubjectPathologies(this.preclinicalSubject).then(pathologies => {
            	if (pathologies){
            		this.pathologies = pathologies;
            	}
                this.browserPaging.setItems(this.pathologies);
            	this.browserPaging.setColumnDefs(this.columnDefs);
                this.table.refresh();
            });
        }else{
        	return new Promise<void> ((resolve) => {
        		resolve();
        	})
        }
    }

 	
    refreshList(newSubPatho: SubjectPathology) {
        this.pathologies = this.pathologies || [];
        this.pathologies.push(newSubPatho);
        this.refreshDisplay(true);
        this.browserPaging.setItems(this.pathologies);
        this.table.refresh();
    }

    refreshDisplay(cancel: boolean) {
        this.toggleFormSP = false;
        this.createSPMode = false;
    }

    checkMode(): void {
        if (this.mode && this.mode.isCreateMode()) {
            this.pathologies = [];
            this.createColumnDefs();
        } else if (this.mode && !this.mode.isCreateMode()) {
            this.getSubjectPathologies();
            this.createColumnDefs();
        } else {
            this.mode.viewMode();
            this.createColumnDefs();
        }
        //SET ADD MODE TO FALSE AS DEFAULT
        //this.addMode = false;
    }

    delete(pathology: SubjectPathology): void {
        this.subjectPathologyService.delete(this.preclinicalSubject, pathology).subscribe();
        this.pathologies = this.pathologies.filter(h => h !== pathology);
    }
    /*
    edit(pathology: SubjectPathology): void {
      this.pathoSelected = pathology;
      this.pathoSelected.id = pathology.id;
      this.pathoSelected.pathology.id = pathology.pathology.id;
      this.pathoSelected.pathology.name = pathology.pathology.name;
      this.pathoSelected.pathologyModel.id = pathology.pathologyModel.id;
      this.pathoSelected.pathologyModel.name = pathology.pathologyModel.name;
      this.pathoSelected.location.reftype = pathology.location.reftype;
      this.pathoSelected.location.value = pathology.location.value;
      //this.addMode = true;
      //this.pathoSelected = this.onSelected.emit(pathology);
    }
    */
    
    ngOnChanges() {
        this.checkMode();
    }

    viewSubjectPathology = (pathology: SubjectPathology) => {
        //this.router.navigate(['/preclinical-pathologies-edit/', pathology.id]);
        this.toggleFormSP = true;
        this.createSPMode = false;
        this.pathoSelected = pathology;
    }

    toggleSubjectPathologyForm() {
        if (this.toggleFormSP == false) {
            this.toggleFormSP = true;
        } else if (this.toggleFormSP == true) {
            this.toggleFormSP = false;
        } else {
            this.toggleFormSP = true;
        }
        this.createSPMode = true;
        this.pathoSelected = new SubjectPathology();
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
        function checkNullValueReference(reference: any) {
            if (reference) {
                return reference.value;
            }
            return '';
        };

        this.columnDefs = [
            { headerName: "Pathology", field: "pathology.name" },
            { headerName: "PathologyModel", field: "pathologyModel.name" },
            {
                headerName: "Location", field: "location.value", type: "reference", cellRenderer: function(params: any) {
                    return checkNullValueReference(params.data.location);
                }
            },
            {
                headerName: "Start Date", field: "startDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End Date", field: "endDate", type: "date", cellRenderer: function(params: any) {
                    return dateRenderer(params.data.endDate);
                }
            },
        ];
        if (!this.mode.isViewMode() && (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert())) {
            this.columnDefs.push({ headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteSubjectPathologyConfirmDialog },
                { headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: this.viewSubjectPathology, component: this });
        }
        if (!this.keycloakService.isUserGuest() && this.mode.isViewMode()) {
            this.columnDefs.push({ headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, action: this.viewSubjectPathology, component: this });
        }
        this.customActionDefs = [];
        //this.customActionDefs.push({title: "add a pathology", img: ImagesUrlUtil.ADD_ICON_PATH, action: this.toggleSubjectPathologyForm });    
        if(!this.mode.isViewMode()) this.customActionDefs.push({ title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });

        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = { action: this.viewSubjectPathology, component: this };
        }
    }
    
    private onRowClick(item: SubjectPathology) {
        if (!this.keycloakService.isUserGuest()) {
            this.viewSubjectPathology(item);
        }
    }

    openDeleteSubjectPathologyConfirmDialog = (item: SubjectPathology) => {
        this.confirmDialogService
            .confirm('Delete pathology for subject', 'Are you sure you want to delete pathology for this subject ' + item.pathology.name + '?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.delete(item);
                }
            });
    }

    deleteAll = () => {
        let ids: number[] = [];
        for (let subjectPatho of this.pathologies) {
            if (subjectPatho["isSelectedInTable"]) ids.push(subjectPatho.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}