import { Component, ViewContainerRef } from '@angular/core';

@Component({
    selector: 'app-modals',
    template: `<div><ng-content></ng-content></div>`,
    styleUrls: ['modals.component.css'],
})
export class ModalsComponent {

    constructor(public vcRef: ViewContainerRef) {
        
    }
}
