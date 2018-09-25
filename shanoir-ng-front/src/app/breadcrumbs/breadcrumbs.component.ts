import { Component } from '@angular/core';
import { BreadcrumbsService, Step } from './breadcrumbs.service';
import { Router } from '@angular/router';

@Component({
    selector: 'breadcrumbs',
    templateUrl: 'breadcrumbs.component.html',
    styleUrls: ['breadcrumbs.component.css']
})

export class BreadcrumbsComponent {

    constructor(
        private service: BreadcrumbsService,
        private router: Router) { 
    }

    get steps(): Step[] {
        return this.service.steps;
    }

    clickStep(index: number) {
        this.service.clickStep(index);
    }

    goHome() {
        this.service.reset();
        this.router.navigate(['/']);
    }
}