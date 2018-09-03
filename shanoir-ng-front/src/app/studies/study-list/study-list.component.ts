import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { ConfirmDialogComponent } from "../../shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Study } from '../shared/study.model';
import { StudyService } from '../shared/study.service';
import { StudyStatus } from '../shared/study-status.enum';
import { TableComponent } from "../../shared/components/table/table.component";
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { Router } from '@angular/router';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';

@Component({
    selector: 'study-list',
    templateUrl: 'study-list.component.html',
    styleUrls: ['study-list.component.css']
})

export class StudyListComponent {
    public studies: Study[];
    private studiesPromise: Promise<void> = this.getStudies();
    private browserPaging: BrowserPaging<Study>;

    public columnDefs: any[];
    public customActionDefs: any[];

    constructor(
            private confirmDialogService: ConfirmDialogService, 
            private keycloakService: KeycloakService,
            private studyService: StudyService, 
            private viewContainerRef: ViewContainerRef,
            private router: Router) {
        this.createColumnDefs();
    }

    getPage(pageable: FilterablePageable): Promise<Page<Study>> {
        return new Promise((resolve) => {
            this.studiesPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    getStudies(): Promise<void> {
        return this.studyService.getStudies().then(studies => {
            if (studies) {
                this.studies = studies;
                this.browserPaging = new BrowserPaging(studies, this.columnDefs);
            }
        })
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
            { headerName: "Name", field: "name" },
            {
                headerName: "Status", field: "studyStatus", cellRenderer: function (params: any) {
                    return StudyStatus[params.data.studyStatus];
                }
            },
            {
                headerName: "Start date", field: "startDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.startDate);
                }
            },
            {
                headerName: "End date", field: "endDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.endDate);
                }
            },
            {
                headerName: "Subjects", field: "nbSujects", type: "number", width: '30px'
            },
            {
                headerName: "Examinations", field: "nbExaminations", type: "number", width: '30px'
            },
            {
                headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target: "/study", getParams: function (item: any): Object {
                    return { id: item.id, mode: "edit" };
                }
            }
            // ,{ headerName: "", type: "button", img: ImagesUrlUtil.GARBAGE_ICON_PATH, action: this.openDeleteStudyConfirmDialog }
        ];

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new study", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/study", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }
    }

    private onRowClick(study: Study) {
        if (!this.keycloakService.isUserGuest()) {
            this.router.navigate(['/study'], { queryParams: { id: study.id, mode: "view" } });
        }
    }

    openDeleteStudyConfirmDialog = (item: Study) => {
        this.confirmDialogService
            .confirm('Delete study', 'Are you sure you want to delete study ' + item.name + '?',
            this.viewContainerRef)
            .subscribe(res => {
                if (res) {
                    this.deleteStudy(item.id);
                }
            })
    }

    deleteAll = () => {
        let ids: number[] = [];
        for (let study of this.studies) {
            if (study["isSelectedInTable"]) ids.push(study.id);
        }
        if (ids.length > 0) {
            console.log("TODO : delete those ids : " + ids);
        }
    }

    deleteStudy(studyId: number) {
        // Delete studyId and refresh page
        this.studyService.delete(studyId).then((res) => this.getStudies());
    }

}