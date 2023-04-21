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
import { EntityService } from '../../shared/components/entity/entity.abstract.service';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { TableComponent } from '../../shared/components/table/table.component';
import { QualityCard } from '../shared/quality-card.model';
import { QualityCardService } from '../shared/quality-card.service';



@Component({
    selector: 'quality-card-list',
    templateUrl: 'quality-card-list.component.html'
})
export class QualityCardListComponent extends BrowserPaginEntityListComponent<QualityCard> {
    
    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(
            private qualityCardService: QualityCardService) {
                
        super('quality-card');
    }

    getService(): EntityService<QualityCard> {
        return this.qualityCardService;
    }

    getOptions() {
        return {
            new: true,
            view: true, 
            edit: this.keycloakService.isUserAdminOrExpert(), 
            delete: this.keycloakService.isUserAdminOrExpert()
        };
    }

    getEntities(): Promise<QualityCard[]> {
        return this.qualityCardService.getAllAdvanced().quick;
    }

    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
            { headerName: "Name", field: "name" },
            { headerName: "Study", field: 'study.name', defaultField: 'study.id',
			 	route: (qualityCard: QualityCard) => '/study/details/' + qualityCard.study.id
			}
        ];
        return colDef;       
    }

    getCustomActionsDefs(): any[] {
        return [];
    }
}