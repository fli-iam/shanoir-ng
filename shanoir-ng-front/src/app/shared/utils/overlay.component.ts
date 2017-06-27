import { Component, Injectable, ElementRef, Renderer, ViewChild, AfterViewInit } from '@angular/core';

@Component({
    selector: 'confirm-dialog',
    template: `
        <editUser #overlayContent (closing)="closeEditSubject($event)" [hidden]="true"></editUser>
    `
})

@Injectable()
export class OverlayComponent implements AfterViewInit{
    @ViewChild('overlayContent') el: ElementRef;

    constructor(private rd: Renderer) {}

    ngAfterViewInit() {
        // console.log(this.rd);
        // this.el.nativeElement.
    }
    
}