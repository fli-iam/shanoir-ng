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

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { ShanoirError } from '../../shared/models/error.model';
import { Center } from '../shared/center.model';
import { CenterService } from '../shared/center.service';

@Component({
    selector: 'center-list',
    templateUrl: 'center-list.component.html',
    styleUrls: ['center-list.component.css'],
    standalone: false
})

export class CenterListComponent extends BrowserPaginEntityListComponent<Center> {

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(
            private centerService: CenterService) {

        super('center');
        this.manageDelete();
    }

    getService(): EntityService<Center> {
        return this.centerService;
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

    getColumnDefs(): ColumnDefinition[] {
        const columnDefs: ColumnDefinition[] = [
            { headerName: 'Id', field: 'id', type: 'number', width: '30px', defaultSortCol: true},
            { headerName: "Name", field: "name" },
            { headerName: "Town", field: "city" },
            { headerName: "Country", field: "country" }
        ];
        if (this.keycloakService.isUserAdminOrExpert()) {
            columnDefs.push({ headerName: "", type: "button", awesome: "fa-solid fa-microscope", tip: () => { return "Add acq. equip." }, action: item => this.openCreateAcqEquip(item) });
        }
        return columnDefs;
    }

    private openCreateAcqEquip = (center: Center) => {
        const currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(() => {
            this.breadcrumbsService.currentStep.addPrefilled('center', center);
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep, false).subscribe(entity => {
                    center.acquisitionEquipments.push(entity as AcquisitionEquipment);
                })
            );
        });
    }

    private manageDelete() {
        this.subscriptions.push(
            this.onDelete.subscribe(response => {
                if (response.error && response.error instanceof ShanoirError && response.error.code == 422) {
                    const msg: string  = this.buildDeleteErrMsg(response as {entity: Center, error: ShanoirError});
                    this.consoleService.log('warn', msg);
                }
            })
        );
    }

    private buildDeleteErrMsg(response: {entity: Center, error: ShanoirError}): string {
        let isLinkedWithEqpts: boolean = false;
        let isLinkedWithStudies: boolean = false;
        const errDetails = response.error.details?.fieldErrors["delete"];
        for (const errKey in errDetails) {
            if (errDetails[errKey]["givenValue"] == "acquisitionEquipments") {
                isLinkedWithEqpts = true;
            }
            if (errDetails[errKey]["givenValue"] == "studies") {
                isLinkedWithStudies = true;
            }
        }
        let msg: string = 'Center "' + response.entity.name + '" cannot be deleted. It is associated with ';
        if (isLinkedWithEqpts) msg += 'acquisition equipment(s)' ;
        if (isLinkedWithEqpts && isLinkedWithStudies) msg += ' and ';
        if (isLinkedWithStudies) msg += 'study(ies)';
        return msg;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}
