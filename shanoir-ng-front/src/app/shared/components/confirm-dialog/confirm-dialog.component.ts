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

import { SuperPromise } from 'src/app/utils/super-promise';
import { NgIf } from '@angular/common';

@Component({
    selector: 'confirm-dialog',
    templateUrl: 'confirm-dialog.component.html',
    styleUrls: ['confirm-dialog.component.css'],
    imports: [NgIf]
})
export class ConfirmDialogComponent {

    title: string;
    mode: 'confirm' | 'choose' | 'info' | 'error';
    private _message: string;
    link: string;
    buttons: {yes: string, no?: string, cancel?: string};
    private closePromise: SuperPromise<any> = new SuperPromise();

    public get message(): string {
        return this._message;
    }

    public set message(value: string) {
        this._message = value?.split(' ').map(w => w.startsWith('https://') ? '<a target="_blank" href="' + w + '">' + w + '</a>' : w).join(' ');
    }

    public openConfirm(title: string, message?: string, buttons?: {yes: string, cancel: string}): Promise<boolean> {
        this.title = title;
        this.message = message;
        this.buttons = buttons;
        this.mode = 'confirm';
        return this.closePromise;
    }

    public openChoose(title: string, message?: string, buttons?: {yes: string, no: string, cancel?: string}): Promise<'yes' | 'no' | false> {
        this.title = title;
        this.message = message;
        this.buttons = buttons;
        this.mode = 'choose';
        return this.closePromise;
    }

    public openInfo(title: string, message?: string, button?: string): Promise<boolean> {
        this.title = title;
        this.message = message;
        this.buttons = {yes: button, no: null, cancel: null};
        this.mode = 'info';
        return this.closePromise;
    }

    public openError(title: string, message?: string, link?: string): Promise<boolean> {
        this.title = title;
        this.message = message;
        this.link = link;
        this.mode = 'error';
        return this.closePromise;
    }

    public close(answer: any) {
        this.closePromise.resolve(answer); // forces boolean to be returned
    }
}
