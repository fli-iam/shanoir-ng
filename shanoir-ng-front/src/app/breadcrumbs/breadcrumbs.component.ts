import { Component } from '@angular/core';
import { BreadcrumbsService, Step } from './breadcrumbs.service';

@Component({
    selector: 'breadcrumbs',
    templateUrl: 'breadcrumbs.component.html',
    styleUrls: ['breadcrumbs.component.css']
})

export class BreadcrumbsComponent {

    constructor(private service: BreadcrumbsService) { 
    }

    get steps(): Step[] {
        return this.service.steps;
    }

    clickStep(index: number) {
        this.service.clickStep(index);
    }
}