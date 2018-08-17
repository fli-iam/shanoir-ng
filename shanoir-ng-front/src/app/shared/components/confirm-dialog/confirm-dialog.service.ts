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