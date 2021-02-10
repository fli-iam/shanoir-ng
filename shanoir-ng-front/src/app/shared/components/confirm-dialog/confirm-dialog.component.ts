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

@Component({
    selector: 'confirm-dialog',
    templateUrl: 'confirm-dialog.component.html',
    styleUrls: ['confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
    
    public title: string;
    public message: string;
    private closeResolve: (value?: boolean | PromiseLike<boolean>) => void;

    public open(title: string, message: string): Promise<boolean> {
        this.title = title;
        this.message = message;
        return new Promise((resolve, reject) => {
            this.closeResolve = resolve;
        });
    }

    public close(answer: boolean) {
        this.closeResolve(answer == true); // forces boolean to be returned
    }

}