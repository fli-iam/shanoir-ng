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

import { Injectable, ViewContainerRef } from '@angular/core';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';

import { Observable } from 'rxjs';

import { ConfirmDialogComponent } from './confirm-dialog.component';

@Injectable()
export class ConfirmDialogService {

    constructor(private dialog: MatDialog) { }

    public confirm(title: string, message: string, viewContainerRef: ViewContainerRef): Observable<boolean> {
        let dialogRef: MatDialogRef<ConfirmDialogComponent>;
        let config = new MatDialogConfig();
        config.viewContainerRef = viewContainerRef;

        dialogRef = this.dialog.open(ConfirmDialogComponent, config);

        dialogRef.componentInstance.title = title;
        dialogRef.componentInstance.message = message;

        return dialogRef.afterClosed();
    }
    
}