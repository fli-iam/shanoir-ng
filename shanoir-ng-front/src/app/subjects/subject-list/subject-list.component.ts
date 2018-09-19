import { Component, ViewContainerRef } from '@angular/core';
import { Router } from '@angular/router';

import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';

@Component({
    selector: 'subject-list',
    templateUrl: 'subject-list.component.html',
    styleUrls: ['subject-list.component.css']
})

export class SubjectListComponent {
    public subjects: Subject[];
    private subjectsPromise: Promise<void> = this.getSubjects();
    private browserPaging: BrowserPaging<Subject>;
    public columnDefs: any[];
    public customActionDefs: any[];
    
    constructor(
            private subjectService: SubjectService, 
            private confirmDialogService: ConfirmDialogService,
            private viewContainerRef: ViewContainerRef, 
            private keycloakService: KeycloakService,
            private router: Router) {
        this.createColumnDefs();
    }

    // Grid data
    getSubjects(): Promise<void> {
        return this.subjectService.getSubjects().then(subjects => {
            if (subjects) {
                this.subjects = subjects;
                this.browserPaging = new BrowserPaging(subjects, this.columnDefs);
            }
        })
    }

    getPage(pageable: FilterablePageable): Promise<Page<Subject>> {
        return new Promise((resolve) => {
            this.subjectsPromise.then(() => {
                resolve(this.browserPaging.getPage(pageable));
            });
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
            { headerName: "Common Name", field: "name", defaultSortCol: true, defaultAsc: true },
            { headerName: "Sex", field: "sex" },

            {
                headerName: "Birth Date", field: "birthDate", type: "date", cellRenderer: function (params: any) {
                    return dateRenderer(params.data.birthDate);
                }
            },

            { headerName: "Manual HD", field: "manualHemisphericDominance"},
            { headerName: "Language HD", field: "languageHemisphericDominance"},
            { headerName: "Imaged object category", field: "imagedObjectCategory"},
            { headerName: "Personal Comments", field: ""}
        ];

        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.columnDefs.push({
                    headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, action: subject => this.router.navigate(['/subject/edit/' + subject.id])
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, action: subject => this.router.navigate(['/subject/details/' + subject.id])
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new subject.", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/subject/create"
            });
        }
    }

    private onRowClick(subject: Subject) {
        if (!this.keycloakService.isUserGuest()) {
            this.router.navigate(['/subject/details/' + subject.id]);
        }
    }
}
