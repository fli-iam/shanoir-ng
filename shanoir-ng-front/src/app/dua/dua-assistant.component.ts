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

import { Location } from '@angular/common';
import { Component, ElementRef, HostBinding, OnDestroy, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import { Subscription } from 'rxjs';

import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { Mode } from '../shared/components/entity/entity.component.abstract';
import { KeycloakService } from '../shared/keycloak/keycloak.service';
import { ImagesUrlUtil } from '../shared/utils/images-url.util';
import { StudyService } from '../studies/shared/study.service';

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
    protected base64Img: string;
    protected loadedImage: File | null = null;
    protected converting: boolean = false;
    protected showPage: boolean = true;
    private studyName: string;

    @ViewChild('pdfContent', { static: false }) pdfContent!: ElementRef;
    readonly shanoirLogoUrl: string = ImagesUrlUtil.SHANOIR_WHITE_LOGO_PATH;
    @HostBinding('class.not-authenticated') notAuthenticated: boolean = !KeycloakService.auth.loggedIn;

    constructor(
            private formBuilder: FormBuilder, 
            private route: ActivatedRoute,
            private router: Router,
            protected duaService: DuaService,
            private location: Location,
            private studyService: StudyService) {
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
            this.dua = new DuaDocument();
            this.studyService.findStudyIdNamesIcanAdmin().then(studies => {
                let study = studies.find(s => s.id == studyId);
                this.dua.studyId = studyId;
                this.studyName = study.name;
                this.dua.studyName = study.name;
            });
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
            this.studyName,
            this.form.get('url')?.value,
            this.form.get('funding')?.value,
            this.form.get('thanks')?.value,
            this.form.get('papers')?.value,
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

    onImageLoaded(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (!input.files?.length) return;
        const file = input.files[0];
        this.loadedImage = file;
        const reader = new FileReader();
        reader.onload = () => {
            this.duaService.imagePreview = reader.result as string;
        };
        reader.readAsDataURL(file);
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

    private getRelativeY(child: HTMLElement, parent: HTMLElement): number {
        const childRect = child.getBoundingClientRect();
        const parentRect = parent.getBoundingClientRect();
        return childRect.top - parentRect.top;
    }

    /**
     * Wrap every single word inside el, including inside children, into a <span>
     * @param el 
     */
    private wrapTextInSpans(el: HTMLElement): void {
        const walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT, null);
        const nodesToWrap: Text[] = [];
        while (walker.nextNode()) {
            const node = walker.currentNode as Text;
            if (node.textContent?.trim()) {
                nodesToWrap.push(node);
            }
        }
        for (const textNode of nodesToWrap) {
            const parent = textNode.parentNode;
            if (!parent) continue;
            const words = textNode.textContent!.split(/(\s+)/); 
            const fragment = document.createDocumentFragment();
            for (const word of words) {
                if (word.trim()) {
                    const span = document.createElement('span');
                    span.classList.add('word');
                    span.textContent = word;
                    fragment.appendChild(span);
                } else {
                    fragment.appendChild(document.createTextNode(word));
                }
            }
            parent.replaceChild(fragment, textNode);
        }
    }

    /**
     * Insert spacer that act like page breaks. 
     * Works by looking at the position of every word.
     */
    private insertPageBreaks(element: HTMLElement) {
        this.wrapTextInSpans(element);
        let page: number = 1;
        const margin: number = 70;
        const pHeight: number = 1132;
        Array.from(element.querySelectorAll('span.word')).forEach(wordEl => {
            const wordBottomY: number = this.getRelativeY((wordEl as HTMLElement), element) + (wordEl as HTMLElement).offsetHeight;
            const nextYLimit: number = (pHeight * page) - margin;
            const overflow: number = wordBottomY - nextYLimit;
            if (overflow > 0) {
                this.addSpacer(wordEl as HTMLElement, margin, overflow);
                page++;
            }
        })

    }

    private addSpacer(wordEl: HTMLElement, margin: number, overflow: number) {
        const div = document.createElement('div');
        div.style.width = '100%';
        div.style.height = (margin*2) + (wordEl.offsetHeight - overflow) + 'px';
        div.style.margin = '0';
        div.classList.add('spacer');
        wordEl.prepend(div);
    }

    protected generatePDF(): void {
        const element = this.pdfContent.nativeElement;

        let originalHtml: string | null = null;
        originalHtml = element.innerHTML;
        this.insertPageBreaks(element);
        const pdf = new jsPDF('p', 'mm', 'a4');
        const pageWidth = pdf.internal.pageSize.getWidth();
        const pageHeight = pdf.internal.pageSize.getHeight();

        this.converting = true;
        setTimeout(() => {
            html2canvas(element, {
                scale: 2,
                useCORS: true,
                allowTaint: true,
            }).then(canvas => {
                const imgData = canvas.toDataURL('image/jpeg', 1.0);
                const imgProps = pdf.getImageProperties(imgData);
                const pdfWidth = pageWidth;
                const pdfHeight = (imgProps.height * pdfWidth) / imgProps.width;
                let position = 0;
                while (position < pdfHeight) {
                    const remainingHeight = pdfHeight - position;
                    const pageContentHeight = Math.min(pageHeight, remainingHeight);
                    pdf.addImage(imgData, 'JPEG', 0, -position, pdfWidth, pdfHeight);
                    position += pageHeight;
                    if (position < pdfHeight) pdf.addPage();
                }
                pdf.save('dua.pdf');
                this.converting = false;
                this.restorePage();
            }).catch(() => {
                this.converting = false;
                this.restorePage();
            });
        });
    }

    private restorePage() {
        this.showPage = false;
        setTimeout(() => {
            this.showPage = true;
        });
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
        confirmDialogService.choose('Data User Agreement',
                'A Data User Agreement is strongly recommended for your study. '
                + 'Once set up it will be mandatory for any study member to agree it before accessing to the data. '
                + 'Do you want to start the dua creation assistant ?',
            {yes: 'Yes', no: 'No'}
        ).then(userChoice => {
            if (userChoice == 'yes') {
                router.navigate(['/dua/create/' + studyId]);
            }
        });
    }

    goBack() {
        this.location.back();
    }

}