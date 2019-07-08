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

import { Component, ViewChild } from '@angular/core';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { ShanoirError } from '../../shared/models/error.model';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'center-list',
    templateUrl: 'center-list.component.html',
    styleUrls: ['center-list.component.css']
})

export class CenterListComponent extends BrowserPaginEntityListComponent<Center> {

    @ViewChild('table') table: TableComponent;
    
    constructor(
            private centerService: CenterService) {

        super('center');
        this.manageDelete();
    }

    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    getEntities(): Promise<Center[]> {
        return this.centerService.getAll(); 
    }

    getColumnDefs() {
        let columnDefs: any[] = [
            { headerName: "Name", field: "name" },
            { headerName: "Town", field: "city" },
            { headerName: "Country", field: "country" }
        ];
        if (this.keycloakService.isUserAdminOrExpert()) {
            columnDefs.push({ headerName: "", type: "button", awesome: "fa-podcast", tip: "Add acq. equip.", action: item => this.openCreateAcqEquip(item) });
        }
        return columnDefs;
    }

    private openCreateAcqEquip = (center: Center) => {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(success => {
            this.breadcrumbsService.currentStep.addPrefilled('center', center);
            currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                center.acquisitionEquipments.push(entity as AcquisitionEquipment);
            });
        });
    }

    private manageDelete() {
        this.subscribtions.push(
            this.onDelete.subscribe(response => {
                if (response instanceof ShanoirError && response.code == 422) {
                    let msg: string  = this.buildDeleteErrMsg(response.details.fieldErrors["delete"] || '');
                    this.msgBoxService.log('warn', msg, 10000); 
                }
            })
        );
    }

    private buildDeleteErrMsg(errDetails: any): string {
        let isLinkedWithEqpts: boolean = false;
        let isLinkedWithStudies: boolean = false;
        for (var errKey in errDetails) {
            if (errDetails[errKey]["givenValue"] == "acquisitionEquipments") {
                isLinkedWithEqpts = true;
            }
            if (errDetails[errKey]["givenValue"] == "studies") {
                isLinkedWithStudies = true;
            }
        }
        let msg: string = 'This center cannot be deleted. It is associated with ';
        if (isLinkedWithEqpts) msg += 'acquisition equipment(s)' ;
        if (isLinkedWithEqpts && isLinkedWithStudies) msg += ' and ';
        if (isLinkedWithStudies) msg += 'study(ies)';
        return msg;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}