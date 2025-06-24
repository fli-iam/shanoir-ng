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

import { Component, ElementRef, OnDestroy, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import * as html2pdf from 'html2pdf.js';
import { Subscription } from 'rxjs';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { Mode } from '../shared/components/entity/entity.component.abstract';
import { DuaDocument } from './shared/dua-document.model';
import { DuaService } from './shared/dua.service';


@Component({
    selector: 'dua-assistant',
    templateUrl: 'dua-assistant.component.html',
    styleUrls: ['dua-assistant.component.css'],
    standalone: false
})

export class DUAAssistantComponent implements OnDestroy {

    protected form: FormGroup;
    private studyId: number;
    protected link: string;
    protected mode: Mode;
    protected dua: DuaDocument;
    protected id: string;
    protected subscriptions: Subscription[] = [];
    @ViewChild('pdfContent', { static: false }) pdfContent!: ElementRef;

    constructor(
            private formBuilder: FormBuilder, 
            private route: ActivatedRoute,
            private duaService: DuaService) {
        this.subscriptions.push(this.route.params.subscribe(
            params => {
                let studyIdStr: string = params['studyId'];
                let studyId: number = studyIdStr ? parseInt(studyIdStr) : null;
                let duaId: string = params['id'];
                let mode: Mode = this.route.snapshot.data['mode'];
                this.init(mode, duaId, studyId);
            })
        );
    }

    private init(mode: Mode, id: string, studyId: number) {
        this.link = null;
        this.form = null;
        this.dua = null;
        this.mode = mode;
        this.studyId = studyId;
        this.id = id;
        if (this.mode == 'create') {
            this.buildForm();
        } else if (this.mode == 'edit') {
            this.duaService.get(id).then(dua => {
                this.buildForm(dua);
            });
        } else if (this.mode == 'view') {
            this.duaService.get(id).then(dua => {
                this.dua = dua;
            });
        }
    }

    protected buildForm(dua?: DuaDocument) {
        let controls: any = {
            'url': [dua?.url, [Validators.required]],
            'funding': [dua?.funding, [Validators.required]],
            'thanks': [dua?.thanks, [Validators.required]],
            'papers': [dua?.papers, [Validators.required]],
        };
        if (this.mode == 'create') {
            controls['email'] = ['', [Validators.email]];
        }
        this.form = this.formBuilder.group(controls);
    }

    protected onSubmit() {
        let dua: DuaDocument = DuaDocument.buildInstance(
            this.studyId,
            this.form.get('url')?.value,
            this.form.get('funding')?.value,
            this.form.get('thanks')?.value,
            this.form.get('papers')?.value
        );
        this.duaService.create(dua, this.form.get('email')?.value)
            .then(id => {
                this.link = '/shanoir-ng/dua/view/' + id;
            });
    }

    protected generatePDF(): void {
        const element = this.pdfContent.nativeElement;
        const options = {
            margin: 10,
            filename: 'mon-document.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2 },
            jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' },
        };
        html2pdf().from(element).set(options).save();
    }

    ngOnDestroy() {
        for (let subscribtion of this.subscriptions) {
            subscribtion.unsubscribe();
        }
    }

    public static openCreateDialog(studyId: number, confirmDialogService: ConfirmDialogService, router: Router) {
        confirmDialogService.confirm('Data User Agreement',
            'A Data User Agreement is strongly recommended for your study. '
            + 'Once set up it will be mandatory for any study member to agree it before accessing to the data. '
            + 'Do you want to start setting up one ?')
            .then(userChoice => {
                if (userChoice) {
                    router.navigate(['/dua/create/' + studyId]);
                }
            });
    }

}