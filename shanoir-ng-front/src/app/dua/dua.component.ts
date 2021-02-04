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

import { Component } from '@angular/core';

import { BreadcrumbsService } from '../breadcrumbs/breadcrumbs.service';
import { ConfirmDialogService } from '../shared/components/confirm-dialog/confirm-dialog.service';
import { KeycloakService } from '../shared/keycloak/keycloak.service';

@Component({
    selector: 'dua',
    templateUrl: 'dua.component.html',
    styleUrls: ['dua.component.css'] 
})

export class DUAComponent {

    checked: boolean = false;

    constructor(
            private breadcrumbsService: BreadcrumbsService,
            private confirmService: ConfirmDialogService) {
        this.breadcrumbsService.markMilestone();
        this.breadcrumbsService.nameStep('DUA');
    }

    refuse() {
        this.confirmService.confirm('Warning !', 'Do you really want to refuse the Data User Agreement for the study xxxx ? You will be removed from this study and won\'t be asked again.');
    }

}