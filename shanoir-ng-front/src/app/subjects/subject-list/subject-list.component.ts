import { Component, ViewChild, ViewContainerRef } from '@angular/core';

import { ConfirmDialogComponent } from "../../shared/components/confirm-dialog/confirm-dialog.component";
import { ConfirmDialogService } from "../../shared/components/confirm-dialog/confirm-dialog.service";
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { KeycloakService } from "../../shared/keycloak/keycloak.service";
import { TableComponent } from "../../shared/components/table/table.component";
import { Subject } from '../shared/subject.model';
import { SubjectService } from '../shared/subject.service';

@Component({
    selector: 'subject-list',
    templateUrl: 'subject-list.component.html',
    styleUrls: ['subject-list.component.css']
})

export class SubjectListComponent {
    public subjects: Subject[];
    public columnDefs: any[];
    public customActionDefs: any[];
    public deletionInternalError: boolean = false;
    public loading: boolean = false;
    public rowClickAction: Object;
    public visible = false;
    private visibleAnimate = false;
    
    constructor(private subjectService: SubjectService, private confirmDialogService: ConfirmDialogService,
        private viewContainerRef: ViewContainerRef, private keycloakService: KeycloakService) {
        this.getSubjects();
        this.createColumnDefs();
    }

    // Grid data
    getSubjects(): void {
        this.loading = true;
        this.subjectService.getSubjects().then(subjects => {
            if (subjects) {
                this.subjects = subjects;
            }
            this.loading = false;
        })
            .catch((error) => {
                // TODO: display error
                this.subjects = [];
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
            { headerName: "Common Name", field: "name" },
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
            this.columnDefs.push(
                {
                    headerName: "", type: "button", img: ImagesUrlUtil.EDIT_ICON_PATH, target: "/subject", getParams: function (item: any): Object {
                        return { id: item.id, mode: "edit" };
                    }
                });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.columnDefs.push({
                headerName: "", type: "button", img: ImagesUrlUtil.VIEW_ICON_PATH, target: "/subject", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            });
        }

        this.customActionDefs = [];
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.customActionDefs.push({
                title: "new subject.", img: ImagesUrlUtil.ADD_ICON_PATH, target: "/subject", getParams: function (item: any): Object {
                    return { mode: "create" };
                }
            });
        }
        if (!this.keycloakService.isUserGuest()) {
            this.rowClickAction = {
                target: "/subject", getParams: function (item: any): Object {
                    return { id: item.id, mode: "view" };
                }
            };
        }
    }


    public show(): void {
        this.visible = true;
        setTimeout(() => this.visibleAnimate = true, 100);
    }

    public hide(): void {
        this.visibleAnimate = false;
        setTimeout(() => this.visible = false, 300);
    }

    public onContainerClicked(event: MouseEvent): void {
        if ((<HTMLElement>event.target).classList.contains('modal')) {
            this.hide();
        }
    }

}
