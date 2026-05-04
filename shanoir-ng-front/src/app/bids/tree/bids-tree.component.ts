/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */
import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Component, ElementRef, Input, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';

import { DatasetService } from 'src/app/datasets/shared/dataset.service';
import { StudyService } from 'src/app/studies/shared/study.service';
import { Task, TaskStatus } from 'src/app/async-tasks/task.model';
import { NotificationsService } from 'src/app/shared/notifications/notifications.service';

import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { GlobalService } from '../../shared/services/global.service';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import * as AppUtils from '../../utils/app.utils';
import { BidsElement } from '../model/bidsElement.model';

@Component({
    selector: 'bids-tree',
    templateUrl: 'bids-tree.component.html',
    styleUrls: ['bids-tree.component.css'],
    standalone: false
})

export class BidsTreeComponent implements OnDestroy, OnInit {

    API_URL = AppUtils.BACKEND_API_BIDS_URL;
    @Input() studyId: number;
    @Input() studyName: string;
    public list: BidsElement[];
    public json: JSON;
    public tsv: string[][];
    public title: string;
    public selectedIndex: string;
    private globalClickSubscription: Subscription;
    public load: string;
    private hasDownloadRight: boolean;
    protected report: any; // JSON
    protected truncatedReport: any; // JSON
    private subscriptions: Subscription[] = [];
    protected task: Task;
    buildingReport: boolean = false;

    constructor(private globalService: GlobalService,
                private elementRef: ElementRef,
                private datasetService: DatasetService,
                protected http: HttpClient,
                private keycloakService: KeycloakService,
                private studyRightsService: StudyRightsService,
                private studyService: StudyService,
                private notificationsService: NotificationsService) {

        this.subscriptions.push(globalService.onGlobalClick.subscribe(clickEvent => {
            if (!this.elementRef.nativeElement.contains(clickEvent.target)) {
                this.selectedIndex = null;
                this.removeContent();
            }
        }));
        this.subscriptions.push(
            this.notificationsService.getNotifications().subscribe(tasks => {
                this.task = tasks.find(t => 
                        t.id === this.task?.id ||
                        (
                            t.eventType === 'bidsExport.event'
                            && t.objectId === this.studyId
                            && [TaskStatus.IN_PROGRESS, TaskStatus.QUEUED, TaskStatus.IN_PROGRESS_BUT_WARNING].includes(t.status))
                        );               
            })
        );
    }

    ngOnInit(): void {
        this.studyRightsService.getMyRightsForStudy(this.studyId).then(rights => {
            this.hasDownloadRight = rights.includes(StudyUserRight.CAN_DOWNLOAD);
        })
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(subscription => subscription.unsubscribe());
    }

    getBidsStructure(): Promise<void> {
       if (!this.load) {
            this.load="loading";
            return this.datasetService.getBidsStructure(this.studyId).then(element => {
                this.sort(element);
                this.list = [element];
            }).finally(() => {
                this.load = "loaded";
            });
        } else {
            return Promise.resolve();
        }
    }

    refresh() {
        this.removeContent();
        this.load = null;
        if (!this.load) {
            this.load ="loading";
            this.datasetService.refreshBidsStructure(this.studyId, this.studyName).then(element => {
                this.sort(element);
                this.list = [element];
                this.load = "loaded";
            })
        }
    }

    sort(element: BidsElement) {
        if (element.elements) {
            element.elements.sort(function(elem1, elem2) {
                if (elem1.file && !elem2.file) {
                    return 1
                } else if (!elem1.file && elem2.file) {
                    return -1;
                } else if (elem1.file && elem2.file || !elem1.file && !elem2.file) {
                    return elem1.path < elem2.path ? -1 : 1;
                }
            });
            // Then sort all sub elements folders
            for (const elem of element.elements) {
                this.sort(elem);
            }
        }
    }

    getFileName(element): string {
        return element.split('\\').pop().split('/').pop();
    }

    getDetail(component: TreeNodeComponent) {
        component.dataLoading = true;
        component.hasChildren = true;
        component.open();
    }

    getContent(bidsElem: BidsElement, id: string) {
        this.removeContent();
        if (id == this.selectedIndex) {
            this.selectedIndex = null;
            return;
        }
        this.selectedIndex = id;
        if (bidsElem.content) {
            this.title = this.getFileName(bidsElem.path);
            if (bidsElem.path.indexOf('.json') != -1) {
                this.json = JSON.parse(bidsElem.content);
            } else if (bidsElem.path.indexOf('.tsv') != -1) {
                this.tsv = this.parseTsv(bidsElem.content);
            } else if (bidsElem.path.indexOf('README') != -1) {
                 this.json = JSON.parse(bidsElem.content);
            }
        }
    }

    private parseTsv(tsv: string): string[][] {
        return tsv.split('\n').map(line => line.split('\t'));
    }

    removeContent() {
        this.title = null;
        this.tsv = null;
        this.json = null;
    }

    public download(item: BidsElement): void {
        const endpoint = this.API_URL + "/exportBIDS/studyId/" + this.studyId;
        const params = new HttpParams().set("filePath", item.path);

        this.http.get(endpoint, { observe: 'response', responseType: 'blob', params: params }).toPromise().then(response => {
            if (response.status == 200) {
                this.downloadIntoBrowser(response);
            }
        });
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        const contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFile(response.body, this.getFilename(response));
    }

    public hasDownloadRights(): boolean {
        return this.keycloakService.isUserAdmin() || this.hasDownloadRight;
    }

    protected callBidsValidator(): void {
        this.buildingReport = true;
        this.report = null;
        this.getBidsStructure().then(() => {
            // currently the list object always contains one element which is the root folder
            return this.studyService.validateStudyForBIDS(this.studyId, this.list[0]?.path).then(report => {
                this.report = report.report;
                this.truncateReportForDisplay();
            });
        }).finally(() => {
            this.buildingReport = false;
        });
    }

    private truncateReportForDisplay(): void {
        const MAX_NB_ISSUES_DISPLAY = 30;
        if (!this.report) {
            this.truncatedReport = null;
            return;
        }
        // Create a deep copy of the report to truncate for display
        this.truncatedReport = JSON.parse(JSON.stringify(this.report));

        // Truncate the "issues" arrays
        const truncateIssues = (obj: any) => {
            if (obj && typeof obj === 'object') {
                for (const key in obj) {
                    if (key === 'issues' && Array.isArray(obj[key])) {
                        const length = obj[key].length;
                        if (length > MAX_NB_ISSUES_DISPLAY) {
                            obj[key] = obj[key].slice(0, MAX_NB_ISSUES_DISPLAY);
                            obj[key].push({ message: `... and ${length - MAX_NB_ISSUES_DISPLAY} more issues not displayed. 
                                You can download the full report for more details.` });
                        }
                    } else {
                        truncateIssues(obj[key]);
                    }
                }
            }
        };

        truncateIssues(this.truncatedReport);
    }

    protected downloadBidsValidatorReport(): void {
        if (!this.report) return;
        const blob = new Blob([JSON.stringify(this.report, null, 4)], {
            type: 'application/json'
        });
        AppUtils.browserDownloadFile(blob, 'bids_validator_' 
            + AppUtils.sanitizeFilename(this.studyName) + '_'
            + new Date().toLocaleString('fr-FR')
            + '.json'
        );
    }
}
