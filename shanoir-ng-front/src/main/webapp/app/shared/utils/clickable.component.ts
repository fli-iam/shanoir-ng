import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';

@Component({
    selector: 'ag-clickable',
    template: `
    <a [routerLink]="['/editUser']" [queryParams]="{id: cell}" routerLinkActive="active" (click)="click()"><img src="/images/edit.16x16.png" /></a>
  `
})
export class ClickableComponent {
    @Input() cell:any;
    @Output() router: Router;
    @Output() onClicked = new EventEmitter<boolean>();

    click() : void {
        this.onClicked.emit(this.cell);
    }
}