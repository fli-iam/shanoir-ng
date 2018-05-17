import { Component, OnInit, SimpleChange, Input } from '@angular/core';

declare var papaya: any;

@Component({
    selector: 'papaya',
    templateUrl: 'papaya.component.html',
    styleUrls: ['papaya.component.css']
})
export class PapayaComponent implements OnInit {

    @Input() params: Object[];


    constructor() {
        this.params = [];
        this.params['kioskMode'] = true;
        this.params['expandable'] = true;
        this.params['allowScroll'] = false;
        this.params['radiological'] = true;
        this.params['showRuler'] = true;
    }

    ngOnInit() {
        papaya.Container.startPapaya();
    }

    ngOnChanges(changes: { [propKey: string]: SimpleChange }) {
        for (let propName in changes) {
            if (!changes[propName].isFirstChange() && propName == 'params') {
                papaya.Container.resetViewer(0, this.params);
            }
        }
    }

} 