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

    getEntities(): Promise<Coil[]> {
        return this.coilService.getAll();
    }

    getColumnDefs(): any[] {
        let colDef: any[] = [
            { headerName: "Name", field: "name" },
            
            { headerName: "Acquisition Equipment Model", field: "manufacturerModel.name" , type: "link", clickAction: {
                target: "/manufacturer-model", getParams: function (coil: Coil): Object {
                    return { id: coil.manufacturerModel.id , mode: "view" };
                }

            } },
           
            { headerName: "Center", field: "center.name" , type: "link", clickAction: {
                target: "/center", getParams: function (coil: Coil): Object {
                    return { id: coil.center.id, mode: "view" };
                }
            }},

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