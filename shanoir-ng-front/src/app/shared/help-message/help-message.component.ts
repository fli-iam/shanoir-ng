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
import { ImportDataService } from '../../import/shared/import.data-service';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';

@Component({
    selector: 'help-message',
    templateUrl: 'help-message.component.html',
    styleUrls: ['help-message.component.css']
})
export class HelpMessageComponent implements OnInit {

    @Input() help: "institution" | "equipment";
    private message: any;
    private inImport: boolean;
    
    constructor(private importDataService: ImportDataService, private breadcrumbsService: BreadcrumbsService) {}

    ngOnInit() {
        if (this.importDataService.patients && this.importDataService.patients[0]
            && this.importDataService.patients[0].studies && this.importDataService.patients[0].studies[0]
            && this.importDataService.patients[0].studies[0].series && this.importDataService.patients[0].studies[0].series[0]) {
            
                if (this.help == 'institution' && this.importDataService.patients[0].studies[0].series[0].institution) {   
                this.message = this.importDataService.patients[0].studies[0].series[0].institution.institutionName + ", " +
                    this.importDataService.patients[0].studies[0].series[0].institution.institutionAddress;
            } else if (this.help == 'equipment' && this.importDataService.patients[0].studies[0].series[0].equipment) {
                this.message = this.importDataService.patients[0].studies[0].series[0].equipment.manufacturer + " - " +
                    this.importDataService.patients[0].studies[0].series[0].equipment.manufacturerModelName + " - " + 
                    this.importDataService.patients[0].studies[0].series[0].equipment.deviceSerialNumber;
            }
        }
        this.inImport = this.breadcrumbsService.isImporting();
    }
}