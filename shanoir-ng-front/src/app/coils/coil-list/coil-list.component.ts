import { Component, ViewChild } from '@angular/core';

import { EntityListComponent } from '../../shared/components/entity/entity-list.component.abstract';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { Coil } from '../shared/coil.model';
import { CoilService } from '../shared/coil.service';
import { TableComponent } from '../../shared/components/table/table.component';

@Component({
    selector: 'coil-list',
    templateUrl: 'coil-list.component.html',
    styleUrls: ['coil-list.component.css'],
})
export class CoilListComponent extends EntityListComponent<Coil> {
    
    @ViewChild('table') table: TableComponent;
    private coilsPromise: Promise<void> = this.getCoils();
    private browserPaging: BrowserPaging<Coil>;
    private coils: Coil[];

    constructor(
            private coilService: CoilService) {
                
        super('coil');
    }

    getPage(pageable: FilterablePageable): Promise<Page<Coil>> {
        return new Promise((resolve) => {
            this.coilsPromise.then(() => {
                this.browserPaging.setItems(this.coils);
                resolve(this.browserPaging.getPage(pageable));
            });
        });
    }

    private getCoils(): Promise<void> {
        return this.coilService.getCoils().then(coils => {
            if (coils) {
                this.coils = coils;
                this.browserPaging = new BrowserPaging(this.coils, this.columnDefs);
            }
        })
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

    onDelete(entity: Coil) {
        this.coils = this.coils.filter(coil => coil.id != entity.id);
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}