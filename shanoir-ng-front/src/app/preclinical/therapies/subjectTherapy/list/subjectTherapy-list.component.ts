import {Component, Input, Output, EventEmitter, ViewChild, ViewContainerRef, OnChanges, ChangeDetectionStrategy} from '@angular/core'
import { Router } from '@angular/router';

import { ConfirmDialogService } from "../../../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from "../../../../shared/keycloak/keycloak.service";

import { SubjectTherapy } from '../shared/subjectTherapy.model';
import { SubjectTherapyService } from '../shared/subjectTherapy.service';

import { PreclinicalSubject } from '../../../animalSubject/shared/preclinicalSubject.model';

import { EnumUtils } from "../../../shared/enum/enumUtils";
import { TherapyType } from "../../../shared/enum/therapyType";
import { Frequency } from "../../../shared/enum/frequency";

import { Mode } from "../../../shared/mode/mode.model";
import { Modes } from "../../../shared/mode/mode.enum";
import { ModesAware } from "../../../shared/mode/mode.decorator";

import { ImagesUrlUtil } from '../../../../shared/utils/images-url.util';

@Component({
    selector: 'subject-therapy-list',
    templateUrl: 'subjectTherapy-list.component.html',
    styleUrls: ['subjectTherapy-list.component.css'],
    providers: [SubjectTherapyService]
})
@ModesAware
export class SubjectTherapiesListComponent {
    subjectTherapies: SubjectTherapy[];
    public loading: boolean = false;
    public rowClickAction: Object;
    public columnDefs: any[];
    public customActionDefs: any[];
    @Input() mode:Mode = new Mode();
    @Input() canModify: Boolean = false;
    @Input() preclinicalSubject: PreclinicalSubject;
    public toggleFormST: boolean = false;
    public createSTMode: boolean = false;
    public therapySelected: SubjectTherapy;


    constructor(
        public subjectTherapyService: SubjectTherapyService,
        public router: Router,
        private keycloakService: KeycloakService,
        public confirmDialogService: ConfirmDialogService, private viewContainerRef: ViewContainerRef) {
            
            this.checkMode();
    }

    getSubjectTherapies(): void {
        this.loading = true;
        if (this.preclinicalSubject && this.preclinicalSubject.animalSubject && this.preclinicalSubject.animalSubject.id) {
            this.subjectTherapyService.getSubjectTherapies(this.preclinicalSubject).then(subjectTherapies => {
                if (subjectTherapies) {
                    this.subjectTherapies = subjectTherapies;
                } else {
                    this.subjectTherapies = [];
                }
                this.loading = false;
            }).catch((error) => {
                this.subjectTherapies = [];
            });
        }
    }

    refreshList(newSubTherapy: SubjectTherapy) {
        this.subjectTherapies = this.subjectTherapies || [];
        this.subjectTherapies.push(newSubTherapy);
        this.refreshDisplay(true);
    }

    refreshDisplay(cancel: boolean) {
        this.toggleFormST = false;
        this.createSTMode = false;
    }

    checkMode(): void {
        if(this.mode && this.mode.isCreateMode()){
            this.loading = false;
            this.subjectTherapies = [];
            this.createColumnDefs();
        }else if(this.mode && !this.mode.isCreateMode()){
            this.getSubjectTherapies();
            this.createColumnDefs();
        }else{
            this.mode.viewMode();
            this.createColumnDefs();
        }
    }

    delete(therapy: SubjectTherapy): void {
        this.subjectTherapyService.delete(this.preclinicalSubject, therapy).then((res) => this.getSubjectTherapies());
    }


    ngOnChanges() {
        this.checkMode();
    }

    viewSubjectTherapy = (therapy: SubjectTherapy) => {
        this.toggleFormST = true;
        this.createSTMode = false;
        this.therapySelected = therapy;
    }

    toggleSubjectTherapyForm() {
        if (this.toggleFormST == false) {
            this.toggleFormST = true;
        } else if (this.toggleFormST == true) {
            this.toggleFormST = false;
        } else {
            this.toggleFormST = true;
        }
        this.createSTMode = true;
        this.therapySelected = new SubjectTherapy();
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
            if (value) {
                return value;
            }
            return '';
        };
        function checkNullValueReference(reference: any) {
            if (reference) {
                return reference.value;
            }
            return '';
        };

        this.columnDefs = [
            /*{headerName: "ID", field: "id", type: "id", cellRenderer: function (params: any) {
                return castToString(params.data.id);
            }}, */
            { headerName: "Therapy", field: "therapy.name" },
            {
                headerName: "Type", field: "therapy.therapyType", type: "Enum", cellRenderer: function(params: any) {
                    return TherapyType[params.data.therapy.therapyType];
                }
            },
            {
                headerName: "Dose", field: "dose", type: "dose", cellRenderer: function(params: any) {
                    return checkNullValue(params.data.dose);
                }
            },
            {
                headerName: "Dose Unit", field: "dose_unit.value", type: "reference", cellRenderer: function(params: any) {
                    return checkNullValueReference(params.data.dose_unit);
                }
            },
            {
                headerName: "Type", field: "frequency", type: "Enum", cellRenderer: function (params: any) {
                    return Frequency[params.data.frequency];
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
            }
        ];
        if (!this.mode.isViewMode() && (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert())) {
            this.columnDefs.push({ headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteSubjectTherapyConfirmDialog },
                { headerName: "", type: "button", img:ImagesUrlUtil.EDIT_ICON_PATH, action: this.viewSubjectTherapy, component: this });
        }
        if (!this.keycloakService.isUserGuest()  && this.mode.isViewMode()) {
            this.columnDefs.push({ headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, action: this.viewSubjectTherapy, component: this });
        }
        this.customActionDefs = [];
        if(!this.mode.isViewMode()) this.customActionDefs.push({ title: "delete selected", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.deleteAll });

        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = { action: this.viewSubjectTherapy, component: this };
        }
    }

    openDeleteSubjectTherapyConfirmDialog = (item: SubjectTherapy) => {
        this.confirmDialogService
            .confirm('Delete therapy for subject', 'Are you sure you want to delete therapy for this subject ' + item.therapy.name + '?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.delete(item);
                }
            });
    }
    
    deleteAll = () => {
        let ids: number[] = [];
        for (let subjectTherapy of this.subjectTherapies) {
            if (subjectTherapy["isSelectedInTable"]) ids.push(subjectTherapy.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

}