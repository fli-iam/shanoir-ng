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

import { Component, Input, OnInit } from '@angular/core';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';

@Component({
    selector: 'help-message',
    templateUrl: 'help-message.component.html',
    styleUrls: ['help-message.component.css']
})
export class HelpMessageComponent implements OnInit {

    @Input() help: "institution" | "equipment";
    message: any;
    inImport: boolean;
    
    constructor(private breadcrumbsService: BreadcrumbsService) {}

    ngOnInit() {
        this.inImport = this.breadcrumbsService.isImporting();
    }
}