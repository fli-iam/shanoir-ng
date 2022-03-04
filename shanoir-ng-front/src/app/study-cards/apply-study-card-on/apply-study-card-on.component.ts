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
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { ManufacturerModel } from '../../acquisition-equipments/shared/manufacturer-model.model';
import { BreadcrumbsService } from '../../breadcrumbs/breadcrumbs.service';
import { DatasetAcquisition } from '../../dataset-acquisitions/shared/dataset-acquisition.model';
import { DatasetAcquisitionService } from '../../dataset-acquisitions/shared/dataset-acquisition.service';
import { DatasetService } from '../../datasets/shared/dataset.service';
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { slideRight } from '../../shared/animations/animations';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { Option } from '../../shared/select/select.component';
import { StudyCard } from '../shared/study-card.model';
import { StudyCardService } from '../shared/study-card.service';

@Component({
    selector: 'apply-study-card-on',
    templateUrl: 'apply-study-card-on.component.html',
    styleUrls: ['apply-study-card-on.component.css'],
    animations: [ slideRight ]
})
export class ApplyStudyCardOnComponent implements OnInit {

    datasetAcquisitions: DatasetAcquisition[];
    selectedAcquisitionIds: Set<number> = new Set();
    studycard: StudyCard;
    studyCards: StudyCard[];
    studycardOptions: Option<StudyCard>[];
    columnsDefs: any = this.getColumnDefs();
    subRowsDefs: any = this.getSubRowDefs();
    customActionDefs: any[] = this.getCustomActionsDefs();
    browserPaging: BrowserPaging<DatasetAcquisition>;
    nbSelectedDatasets: number;
    nbIncompatible: number = 0;


    constructor(
            private datasetService: DatasetService,
            private datasetAcquisitionService: DatasetAcquisitionService,
            private studycardService: StudyCardService,
            private activatedRoute: ActivatedRoute,
            private breadcrumbsService: BreadcrumbsService,
            private confirmService: ConfirmDialogService,
            private router : Router) {
                
        breadcrumbsService.nameStep('Reapply Study Card');

    }

    ngOnInit(): void {
        let datasetIds: number[] = this.breadcrumbsService.currentStep.data.datasetIds;
        if (!datasetIds || datasetIds.length == 0) {
            this.router.navigate(['/solr-search']);
            return;
        }
        
        let acquisitionPromise: Promise<DatasetAcquisition[]> = this.datasetAcquisitionService.getAllForDatasets(datasetIds).then(dsAcqs => {
            this.datasetAcquisitions = dsAcqs;
            this.browserPaging = new BrowserPaging(dsAcqs, this.columnsDefs);
            this.selectAll();
            return dsAcqs;
        });

        Promise.all([acquisitionPromise, this.studycardService.getAll()]).then(([acquisitions, studycards]) => {
            this.studyCards = studycards;
            this.studycardOptions = [];
            studycards.forEach(sc => {
                if (sc) {
                    let option: Option<StudyCard> = new Option(sc, sc.name);
                    option.compatible = acquisitions.findIndex(acq => acq.acquisitionEquipment?.id != sc.acquisitionEquipment?.id) == -1;
                    this.studycardOptions.push(option);
                }
            });
        });
    }

    selectAll() {
        this.selectedAcquisitionIds = new Set(this.datasetAcquisitions.map(acq => acq.id));
        this.onSelectionChange();
    }

    unSelectAll() {
        this.selectedAcquisitionIds = new Set()
        this.onSelectionChange();
    }


    getPage(pageable: FilterablePageable, forceRefresh: boolean = false): Promise<Page<DatasetAcquisition>> {
        return Promise.resolve(this.browserPaging.getPage(pageable));
    }

    // reapplyOnAll() {
    //     this.reapplyOn(this.getAllDatasetsIds());
    // }

    reapplyOnSelected() {
        this.reapplyOn([...this.selectedAcquisitionIds]);
    }

    private reapplyOn(datasetIds: number[]) {
        this.confirmService.confirm('Apply Study Card ?',
                'Would you like to apply the study card "' 
                + this.studycard.name
                + '" to ' + datasetIds.length
                + ' datasets? Note that any previous study card application will be permanentely overwriten by new values.'
        ).then(res => {
            if (res) {
                this.studycardService.applyStudyCardOn(this.studycard.id, datasetIds);
            }
        });
    }

    getColumnDefs(): any[] {
        let colDef: any[] = [
            { headerName: "Compatible", type: "boolean", cellRenderer: row => this.isCompatible(row.data.acquisitionEquipment?.id), awesome: "fa-solid fa-circle", awesomeFalse: "fa-solid fa-triangle-exclamation", color: "green", colorFalse: "orangered", suppressSorting: true },
            { headerName: 'Id', field: 'id', type: 'number', width: '30px', defaultSortCol: true, defaultAsc: false},
            { headerName: 'Type', field: 'type', width: '22px'},
            { headerName: "Acquisition Equipment", field: "acquisitionEquipment", orderBy: ['acquisitionEquipmentId'],
                cellRenderer: (params: any) => this.transformAcqEq(params.data.acquisitionEquipment),
                route: (dsAcq: DatasetAcquisition) => '/acquisition-equipment/details/' + dsAcq.acquisitionEquipment.id
            },
            { headerName: "Study", field: "examination.study.name", defaultField: 'examination.study.id', orderBy: ['examination.studyId'],
				route: (dsAcq: DatasetAcquisition) => '/study/details/' + dsAcq.examination.study.id},
            { headerName: "Examination date", type: 'date', field: 'examination.examinationDate', cellRenderer: (params: any) => {
                return this.dateRenderer(params.data.examination.examinationDate);
            }},    
            { headerName: "Center", field: "acquisitionEquipment.center.name", suppressSorting: true,
				route: (dsAcq: DatasetAcquisition) => dsAcq.acquisitionEquipment.center? '/center/details/' + dsAcq.acquisitionEquipment.center.id : null
			},
            { headerName: "Last StudyCard", field: "studyCard.name",
                route: (dsAcq: DatasetAcquisition) => dsAcq.studyCard ? '/study-card/details/' + dsAcq.studyCard.id : null
            }
        ];
        return colDef;       
    }

    getSubRowDefs() {
        return [
            {headerName: "Id", field: "id", type: "number", width: "60px", defaultSortCol: true, defaultAsc: false},
            {headerName: "Name", field: "name", orderBy: ["updatedMetadata.name", "originMetadata.name", "id"]},
            {headerName: "Comment", field: "originMetadata.comment"},
        ];
    }

    getCustomActionsDefs(): any[] {
        let customActionDefs:any = [];
        customActionDefs.push(
            {title: "Clear selection", awesome: "fa-solid fa-snowplow", action: () => this.unSelectAll(), disabledIfNoSelected: true},
            {title: "Select all", awesome: "fa-solid fa-square", action: () => this.selectAll()},
        );
        return customActionDefs;
    }

    private transformAcqEq(acqEqpt: AcquisitionEquipment): string {
        if (!acqEqpt) return "";
        else if (!acqEqpt.manufacturerModel) return String(acqEqpt.id);
        else {
            let manufModel: ManufacturerModel = acqEqpt.manufacturerModel;
            return manufModel.manufacturer.name + " - " + manufModel.name + " " + (manufModel.magneticField ? (manufModel.magneticField + "T") : "")
                + " (" + DatasetModalityType.getLabel(manufModel.datasetModalityType) + ") " + acqEqpt.serialNumber
        }
    }

    private dateRenderer(date: number): string {
        if (date) {
            return new Date(date).toLocaleDateString();
        }
        return null;
    };

    onSelectionChange() {
        this.nbSelectedDatasets = 0;
        this.datasetAcquisitions.forEach(acq => {
            if (this.selectedAcquisitionIds.has(acq.id)) {
                this.nbSelectedDatasets += acq.datasets?.length;
            }
        });
        this.updateStudyCardCompatibilities();
        this.updateNbIncompatible();
    }

    showStudyCard() {
        if (this.studycard) {
            this.router.navigate(['/study-card/details/' + this.studycard.id]);
        }
    }

    isCompatible(equipmentId: number): Boolean {
        if (this.studycard) {
            return this.studycard.acquisitionEquipment?.id == equipmentId;
        } else {
            return null;
        }
    }

    updateStudyCardCompatibilities() {
        if (this.studycardOptions) {
            this.studycardOptions.forEach(option => {
                option.compatible = this.datasetAcquisitions
                    .filter(acq => this.selectedAcquisitionIds.has(acq.id))
                    .findIndex(acq => acq.acquisitionEquipment?.id != option.value.acquisitionEquipment?.id) == -1;
            });
        }
    }

    updateNbIncompatible() {
        this.nbIncompatible = 0;
        if (this.datasetAcquisitions && this.studycard) {
            this.datasetAcquisitions.forEach(acq => {
                if (this.selectedAcquisitionIds.has(acq.id) && acq.acquisitionEquipment?.id != this.studycard.acquisitionEquipment?.id) {
                    this.nbIncompatible++;
                } 
            });
        }
    }

}
