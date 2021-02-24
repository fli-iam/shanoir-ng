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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { StudyService } from '../../studies/shared/study.service';
import { browserDownloadFile } from '../../utils/app.utils';
import { DataUserAgreement } from '../shared/dua.model';


@Component({
    selector: 'dua-signing',
    templateUrl: 'dua-signing.component.html',
    styleUrls: ['dua-signing.component.css'] 
})

export class DUASigningComponent implements OnChanges {

    @Input() dua: DataUserAgreement;
    @Output() sign: EventEmitter<void> = new EventEmitter<void>();
    pdfUrl: string;
    checked: boolean = false;
    duaBlob: Blob;

    constructor(
            private breadcrumbsService: BreadcrumbsService,
            private confirmService: ConfirmDialogService,
            private studyService: StudyService,
            private sanitizer: DomSanitizer) {
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['dua']) {
            if (this.dua && this.dua.studyId) {
                this.studyService.downloadBlob(this.dua.path, this.dua.studyId, 'dua').then(response => {
                    this.duaBlob = response.body;
                    let url: SafeResourceUrl = this.sanitizer.bypassSecurityTrustResourceUrl(window.URL.createObjectURL(this.duaBlob));
                    this.pdfUrl = url as string;
                });
            } else {
                this.pdfUrl = null;
            }
        }
    }

    accept() {
        this.studyService.acceptDUA(this.dua.id).then(() => {
            this.sign.emit();
        });
    }

    refuse() {
        let confirmMsg: string = 'Do you really want to refuse the Data User Agreement for the study xxxx ? You will be removed from this study and won\'t be asked again.';
        this.confirmService.confirm('Warning !', confirmMsg).then(response => {
            if (response) {

            } else {

            }
        });
    }

    dlDua() {
        if (this.duaBlob) browserDownloadFile(this.duaBlob, this.dua.path);
    }

}