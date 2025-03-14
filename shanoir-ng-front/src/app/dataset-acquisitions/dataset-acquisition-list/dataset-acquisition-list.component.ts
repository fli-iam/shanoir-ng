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
import { TableComponent } from '../../shared/components/table/table.component';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { DatasetAcquisition } from '../shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from '../shared/dataset-acquisition.service';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { EntityListComponent } from '../../shared/components/entity/entity-list.component.abstract';
import { Pageable, Page } from '../../shared/components/table/pageable.model';
import { EntityService } from '../../shared/components/entity/entity.abstract.service';

@Component({
    selector: 'dataset-acquisition-list',
    templateUrl: 'dataset-acquisition-list.component.html',
    styleUrls: ['dataset-acquisition-list.component.css'],
    standalone: false
})
export class DatasetAcquisitionListComponent extends EntityListComponent<DatasetAcquisition> {

    @ViewChild('table', { static: false }) table: TableComponent;

    constructor(
        private datasetAcquisitionService: DatasetAcquisitionService) {

            super('dataset-acquisition');
        }

    getService(): EntityService<DatasetAcquisition> {
        return this.datasetAcquisitionService;
    }

    getOptions() {
        return {
            new: false,
            view: true,
            edit: this.keycloakService.isUserAdmin(),
            delete: this.keycloakService.isUserAdmin()
        };
    }

    getPage(pageable: Pageable): Promise<Page<DatasetAcquisition>> {
        return this.datasetAcquisitionService.getPage(pageable);
    }

    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
            { headerName: 'Id', field: 'id', type: 'number', width: '30px', defaultSortCol: true, defaultAsc: false},
            { headerName: 'Type', field: 'type', width: '22px', disableSorting: true},
            { headerName: "Center Equipment", field: "acquisitionEquipment", orderBy: ['acquisitionEquipmentId'],
                cellRenderer: (params: any) => this.transformAcqEq(params.data.acquisitionEquipment),
                route: (dsAcq: DatasetAcquisition) => '/acquisition-equipment/details/' + dsAcq.acquisitionEquipment?.id
            },
            { headerName: "Study", field: "examination.study.name", defaultField: 'examination.study.id', orderBy: ['examination.study.name'],
                route: (dsAcq: DatasetAcquisition) => '/study/details/' + dsAcq.examination.study.id},
            { headerName: "Subject", field: "examination.subject.name", defaultField: 'examination.subject.id', orderBy: ['examination.subject.name'],
                route: (dsAcq: DatasetAcquisition) => '/subject/details/' + dsAcq.examination.subject.id},
            { headerName: "Examination date", type: 'date', field: 'examination.examinationDate' },
            { headerName: "Acquisition Center", field: "acquisitionEquipment.center.name", orderBy: ['examination.centerId'],
				route: (dsAcq: DatasetAcquisition) => (dsAcq.acquisitionEquipment && dsAcq.acquisitionEquipment.center) ? '/center/details/' + dsAcq.acquisitionEquipment?.center.id : null
			},
            { headerName: "StudyCard", field: "studyCard.name",
                route: (dsAcq: DatasetAcquisition) => dsAcq.studyCard ? '/study-card/details/' + dsAcq.studyCard.id : null
            }
        ];
        return colDef;
    }

    getCustomActionsDefs(): any[] {
        return [];
    }

    transformAcqEq(acqEqpt: AcquisitionEquipment): string {
        if (!acqEqpt || acqEqpt.id == 0) return "";
        else if (!acqEqpt.manufacturerModel) return String(acqEqpt.id);
        else {
            let manufModel: ManufacturerModel = acqEqpt.manufacturerModel;
            return manufModel.manufacturer.name + " - " + manufModel.name + " " + (manufModel.magneticField ? (manufModel.magneticField + "T") : "")
                + " (" + DatasetModalityType.getLabel(manufModel.datasetModalityType) + ") " + acqEqpt.serialNumber
        }
    }
}
