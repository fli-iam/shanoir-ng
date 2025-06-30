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

import { Component, ElementRef, HostBinding, OnDestroy, ViewChild } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import * as html2pdf from 'html2pdf.js';
import { from, Observable, of, Subscription } from 'rxjs';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { Mode } from '../shared/components/entity/entity.component.abstract';
import { DuaDocument } from './shared/dua-document.model';
import { DuaService } from './shared/dua.service';
import { ImagesUrlUtil } from '../shared/utils/images-url.util';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { HttpClient } from '@angular/common/http';
import { catchError, map } from 'rxjs/operators';


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
    protected base64Img: string;

    @ViewChild('pdfContent', { static: false }) pdfContent!: ElementRef;
    readonly shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
    @HostBinding('class.not-authenticated') notAuthenticated: boolean = !KeycloakService.auth.loggedIn;

    constructor(
            private formBuilder: FormBuilder, 
            private route: ActivatedRoute,
            private router: Router,
            private duaService: DuaService,
            private http: HttpClient) {
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
            'logoUrl': [dua?.logoUrl, {
                asyncValidators: this.corsAllowedValidator(),
                updateOn: 'blur'
            }],
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
            this.form.get('papers')?.value,
            this.form.get('logoUrl')?.value
        );
        if (this.mode == 'create') {
            this.duaService.create(dua, this.form.get('email')?.value)
                .then(id => {
                    this.link = '/shanoir-ng/dua/view/' + id;
                });
        } else if (this.mode == 'edit') {
            dua.id = this.id;
            this.duaService.update(dua)
                .then(() => {
                    this.router.navigate(['/dua/view/' + dua.id]);
                });
        }
    }

    corsAllowedValidator(): AsyncValidatorFn {
        return (control: AbstractControl): Observable<ValidationErrors | null> => {
            const url = control.value;
            if (!url) return of(null); // Skip if empty
            try {
                new URL(url); // Validate URL format
            } catch {
                return of({ invalidUrlFormat: true }); // Malformed URL
            }

            return from(
                fetch(url, { method: 'GET', mode: 'cors' })
                    .then(res => {
                        if (res.status === 404) return { notFound: true }; // 404 not found
                        if (!res.ok) return { httpError: true }; // Other HTTP error (e.g. 403, 500)
                        const contentType = res.headers.get('content-type') || '';
                        if (!contentType.startsWith('image/')) return { notAnImage: true }; // Not an image
                        return null; // All good
                    })
                    .catch(() => {
                        return { corsError: true }; // Likely a CORS or network error
                    })
            );
        };
    }

    formErrors(field: string): any {
        if (!this.form) return;
        const control = this.form.get(field);
        if (control && control.touched && !control.valid) {
            return control.errors;
        }
    }

    hasError(fieldName: string, errors?: string[]) {
        let formError = this.formErrors(fieldName);
        if (formError) {
            if (errors) {
                for (let errorName of errors) {
                    if (formError[errorName]) return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    protected generatePDF(): void {
        const element = this.pdfContent.nativeElement;
        const options = {
            margin: 0,
            padding: 0,
            filename: 'dua.pdf',
            image: { type: 'jpeg', quality: 0.98 },
            html2canvas: { scale: 2, useCORS: true, allowTaint: true },
            jsPDF: { format: [1000, 1360], unit: 'px', orientation: 'portrait' },
        };
        html2pdf().from(element).set(options).save();
    }

    isAuthenticated(): boolean {
        return KeycloakService.auth.loggedIn;
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