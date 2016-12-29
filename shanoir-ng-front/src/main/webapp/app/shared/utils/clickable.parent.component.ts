import {Component} from '@angular/core';

import {AgRendererComponent} from 'ag-grid-ng2/main';

@Component({
    selector: 'clickable-cell',
    template: `
    <ag-clickable [cell]="cell"></ag-clickable>
    `
})
export class ClickableParentComponent implements AgRendererComponent {
    private params:any;
    public cell:any;

    agInit(params:any):void {
        this.params = params;
        this.cell = params.value;
    }
}