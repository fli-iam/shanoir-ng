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

import { Component, HostListener } from '@angular/core';
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
        if (index < this.service.steps.length - 1)
            this.service.goToStep(index);
    }    

    goHome() {
        this.router.navigate(['/home']);
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == 'œ') {
            console.log('breadcrumbs', this.service.steps);
        }
    }
}