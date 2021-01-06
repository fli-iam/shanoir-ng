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

import { BrowserPaginEntityListComponent } from '../../shared/components/entity/entity-list.browser.component.abstract';
import { TableComponent } from '../../shared/components/table/table.component';
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';

@Component({
    selector: 'coil-list',
    templateUrl: 'coil-list.component.html',
    styleUrls: ['coil-list.component.css'],
})
export class CoilListComponent extends BrowserPaginEntityListComponent<Coil> {
    
    @ViewChild('table') table: TableComponent;

    constructor(
            private coilService: CoilService) {
                
        super('coil');
    }

    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    getEntities(): Promise<Coil[]> {
        return this.coilService.getAll();
    }

    getColumnDefs(): any[] {
        let colDef: any[] = [
            { headerName: "Name", field: "name" },
            
            { headerName: "Acquisition Equipment Model", field: "manufacturerModel.name",
            route: (coil: Coil) => '/manufacturer-model/details/' + coil.manufacturerModel.id
            },
           
            { headerName: "Center", field: "center.name",
            route: (coil: Coil) => '/center/details/' + coil.center.id
            },

            { headerName: "Coil Type", field: "coilType" },
            { headerName: "Number of channels", field: "numberOfChannels" },
            { headerName: "Serial number", field: "serialNumber" }
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}