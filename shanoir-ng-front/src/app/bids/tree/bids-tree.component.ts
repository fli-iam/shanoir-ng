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
import { Component, ElementRef, Input, OnDestroy, EventEmitter, Output, ViewChild } from '@angular/core';
import { Subscription } from 'rxjs';

import { Router } from '@angular/router';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { CenterService } from '../../centers/shared/center.service';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { GlobalService } from '../../shared/services/global.service';
import * as AppUtils from '../../utils/app.utils';
import { ServiceLocator } from '../../utils/locator.service';
import { BidsElement } from '../model/bidsElement.model';
import { StudyService } from '../../studies/shared/study.service';
import { Study } from '../../studies/shared/study.model'

@Component({
    selector: 'bids-tree',
    templateUrl: 'bids-tree.component.html',
    styleUrls: ['bids-tree.component.css'],
})

export class BidsTreeComponent implements OnDestroy {

    API_URL = AppUtils.BACKEND_API_BIDS_URL;
    protected http: HttpClient = ServiceLocator.injector.get(HttpClient);

    @Input() study: Study;
    protected list: BidsElement[] = [];
    protected json: JSON;
    protected tsv: string[][];
    protected title: string;
    protected selectedIndex: string;
    private globalClickSubscription: Subscription;
    protected load: string;
    @ViewChild('bidsTree') tree:TreeNodeComponent;

    constructor(private globalService: GlobalService, private elementRef: ElementRef, private studyService: StudyService, protected http: HttpClient) {
        this.globalClickSubscription = globalService.onGlobalClick.subscribe(clickEvent => {
            if (!this.elementRef.nativeElement.contains(clickEvent.target)) {
                this.selectedIndex = null;
                this.removeContent();
            }
        }) 
    }

    ngOnDestroy(): void {
        this.globalClickSubscription.unsubscribe();
    }

    getBidsStructure() {
       if (!this.load) {
        this.load="loading"
            this.studyService.getBidsStructure(this.study.id).then(element => {
                this.sort(element);
                this.list = [element];
                this.load = "loaded";
            });
        }
    }

    updateStructure() {
        const endpoint = this.API_URL + "/updateBids/studyId/" + this.study.id + "/studyName/" + this.study.name;
        this.load = undefined;
        this.list = [];
        this.tree.close();
        this.http.get(endpoint).subscribe(response => {
            this.getBidsStructure();
            this.tree.close();
        });
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
            for (let elem of element.elements) {
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

    protected download(item: BidsElement): void {
        const endpoint = this.API_URL + "/exportBIDS/studyId/" + this.study.id;
        let params = new HttpParams().set("filePath", item.path);
        
        this.http.get(endpoint, { observe: 'response', responseType: 'blob', params: params }).subscribe(response => {
            if (response.status == 200) {
                this.downloadIntoBrowser(response);
            }
        });
    }

    private getFilename(response: HttpResponse<any>): string {
        const prefix = 'attachment;filename=';
        let contentDispHeader: string = response.headers.get('Content-Disposition');
        return contentDispHeader.slice(contentDispHeader.indexOf(prefix) + prefix.length, contentDispHeader.length);
    }

    private downloadIntoBrowser(response: HttpResponse<Blob>){
        AppUtils.browserDownloadFile(response.body, this.getFilename(response));
    }


}