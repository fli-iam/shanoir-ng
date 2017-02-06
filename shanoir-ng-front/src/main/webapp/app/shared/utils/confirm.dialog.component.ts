import { Component } from '@angular/core';
import { MdDialogRef } from '@angular/material';

@Component({
  selector: 'confirm-dialog',
  template: `
    <h2>{{ title }}</h2>
    <p>{{ message }}</p>
    <button type="button" md-raised-button 
        (click)="dialogRef.close(true)">OK</button>
    <button type="button" md-button 
        (click)="dialogRef.close()">Cancel</button>
  `
})
export class ConfirmDialogComponent {
    
    public title: string;
    public message: string;

    constructor(public dialogRef: MdDialogRef<ConfirmDialogComponent>) { }

}