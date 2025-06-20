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

import { Component } from '@angular/core';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DuaDocument } from './shared/dua-document.model';
import { DuaService } from './shared/dua.service';


@Component({
    selector: 'dua-assistant',
    templateUrl: 'dua-assistant.component.html',
    styleUrls: ['dua-assistant.component.css'],
    standalone: false
})

export class DUAAssistantComponent {

    protected form: FormGroup;
    private studyId: number;
    protected link: string;

    constructor(
            private formBuilder: FormBuilder, 
            private route: ActivatedRoute,
            private duaService: DuaService) {
        let studyIdStr: string = this.route.snapshot.paramMap.get('id');
        this.studyId = studyIdStr ? parseInt(this.route.snapshot.paramMap.get(studyIdStr)) : null;
        this.buildForm();
    }

    protected buildForm() {
        this.form = this.formBuilder.group({
            'url': ['', [Validators.required]],
            'funding': ['', [Validators.required]],
            'thanks': ['', [Validators.required]],
            'papers': ['', [Validators.required]],
            'email': ['', [Validators.email]],
        });
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
                this.link = '/dua/view/' + id;
            });
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