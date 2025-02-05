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
import { DatasetModalityType } from '../../enum/dataset-modality-type.enum';
import { slideRight } from '../../shared/animations/animations';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { Option } from '../../shared/select/select.component';
import { StudyCard } from '../shared/study-card.model';
import { StudyCardService } from '../shared/study-card.service';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { Location } from '@angular/common';

export type Status = 'default' | 'loading' | 'done' | 'error';
@Component({
    selector: 'apply-study-card-on',
    templateUrl: 'apply-study-card-on.component.html',
    styleUrls: ['apply-study-card-on.component.css'],
    animations: [slideRight],
    standalone: false
})
export class ApplyStudyCardOnComponent implements OnInit {

    datasetAcquisitions: DatasetAcquisition[];
    nbNonAdminAcquisitions: number;
    nonAdminStudies: Set<string>;
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
    private _status: Status = 'default';
    showIncompatibles: boolean = true;
    studyRights: Map<number, StudyUserRight[]>;
    errorMessage: string;

    constructor(
            private datasetAcquisitionService: DatasetAcquisitionService,
            private studyRightsService: StudyRightsService,
            private studycardService: StudyCardService,
            private breadcrumbsService: BreadcrumbsService,
            private confirmService: ConfirmDialogService,
            private router : Router,
            private keycloakService: KeycloakService,
            private activatedRoute: ActivatedRoute,
            private location: Location) {

        breadcrumbsService.nameStep('Reapply Study Card');

    }

    get status(): Status {
        return this._status;
    }

    set status(status: Status) {
        this._status = status;
        this.breadcrumbsService.currentStep.data.status = status;
    }

    ngOnInit(): void {
        const studyCardId: number = parseInt(this.activatedRoute.snapshot.paramMap.get('id'));
        let datasetIds: number[] = Array.from(this.breadcrumbsService.currentStep.data.datasetIds || []);
        let acquisitionPromise: Promise<DatasetAcquisition[]>;
        let studycardPromise: Promise<StudyCard[] | StudyCard>;
        if (datasetIds?.length > 0) {
            acquisitionPromise = this.datasetAcquisitionService.getAllForDatasets(datasetIds);
            studycardPromise = this.studycardService.getAll();
        } else if (!Number.isNaN(studyCardId)) {
            acquisitionPromise = this.datasetAcquisitionService.getByStudycardId(studyCardId);
            studycardPromise = this.studycardService.get(studyCardId);
        } else {
            this.location.back();
            return;
        }

        let rightsPromise: Promise<Map<number, StudyUserRight[]>> = this.studyRightsService.getMyRights().then(rights => this.studyRights = rights);

        this.status = this.breadcrumbsService.currentStep.data.status ? this.breadcrumbsService.currentStep.data.status : 'default';
        if (this.breadcrumbsService.currentStep.data.showIncompatibles != undefined) {
            this.showIncompatibles = this.breadcrumbsService.currentStep.data.showIncompatibles;
        }

        let filteredAcquisitionsPromise: Promise<DatasetAcquisition[]> = Promise.all([acquisitionPromise, rightsPromise]).then(([acquisitions, rights]) => {
            let nonAdminAcquisitions: DatasetAcquisition[] = this.keycloakService.isUserAdmin() ? [] : acquisitions?.filter(acq =>
                !rights.get(acq.examination?.study?.id)?.includes(StudyUserRight.CAN_ADMINISTRATE)
            );
            this.nonAdminStudies = new Set();
            nonAdminAcquisitions.forEach(acq => {
                this.nonAdminStudies.add(acq.examination?.study?.name);
            });
            this.nbNonAdminAcquisitions = nonAdminAcquisitions?.length;
            this.datasetAcquisitions = this.keycloakService.isUserAdmin() ? acquisitions : acquisitions?.filter(acq =>
                rights.get(acq.examination?.study?.id)?.includes(StudyUserRight.CAN_ADMINISTRATE)
            );
            this.browserPaging = new BrowserPaging(this.datasetAcquisitions, this.columnsDefs);
            this.selectAll();
            return this.datasetAcquisitions;
        });

        Promise.all([filteredAcquisitionsPromise, studycardPromise]).then(([acquisitions, studycards]) => {
            if (Array.isArray(studycards)) {
                this.studyCards = studycards;
                this.updateOptions();
                if (this.breadcrumbsService.currentStep.data.studyCardId) {
                    this.studycard = this.studyCards.find(sc => sc.id == this.breadcrumbsService.currentStep.data.studyCardId);
                }
            } else {
                this.studycard = studycards;
            }
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

    reapplyOnSelected() {
        this.reapplyOn([...this.selectedAcquisitionIds]);
    }

    private reapplyOn(datasetAcquisitionIds: number[]) {
        this.confirmService.confirm('Apply Study Card ?',
                'Would you like to apply the study card "'
                + this.studycard.name
                + '" to ' + datasetAcquisitionIds.length
                + ' datasets? Note that any previous study card application will be permanentely overwriten by new values.'
        ).then(res => {
            if (res) {
                this.status = 'loading';
                this.studycardService.applyStudyCardOn(this.studycard.id, datasetAcquisitionIds).then(() => {
                    this.status = 'done';
                }).catch(error => {
                    this.status = 'error';
                    if (error?.error?.message?.includes('could not get dicom attributes from pacs')) {
                        this.errorMessage = 'Could not get dicom attributes from pacs';
                    }
                    throw error;
                });
            }
        });
    }

    getColumnDefs(): ColumnDefinition[] {
        let colDef: ColumnDefinition[] = [
            { headerName: "Compatible", type: "boolean", cellRenderer: row => this.isCompatible(row.data.acquisitionEquipment?.id), awesome: "fa-solid fa-circle", awesomeFalse: "fa-solid fa-triangle-exclamation", color: "green", colorFalse: "orangered", disableSorting: true },
            { headerName: 'Id', field: 'id', type: 'number', width: '30px', defaultSortCol: true, defaultAsc: false},
            { headerName: 'Type', field: 'type', width: '22px'},
            { headerName: "Center Equipment", field: "acquisitionEquipment", orderBy: ['acquisitionEquipmentId'],
                cellRenderer: (params: any) => this.transformAcqEq(params.data?.acquisitionEquipment),
                route: (dsAcq: DatasetAcquisition) => '/acquisition-equipment/details/' + dsAcq?.acquisitionEquipment?.id
            },
            { headerName: "Study", field: "examination.study.name", defaultField: 'examination.study.id', orderBy: ['examination.studyId'],
				route: (dsAcq: DatasetAcquisition) => '/study/details/' + dsAcq?.examination?.study?.id},
            { headerName: "Examination date", type: 'date', field: 'examination.examinationDate' },
            { headerName: "Acquisition Center", field: "acquisitionEquipment.center.name", disableSorting: true,
				route: (dsAcq: DatasetAcquisition) => dsAcq?.acquisitionEquipment?.center? '/center/details/' + dsAcq?.acquisitionEquipment?.center?.id : null
			},
            { headerName: "Last StudyCard", field: "studyCard.name"},
            { headerName: "", type: "button", awesome: "fa-regular fa-eye", action: item => this.router.navigate(['/dataset-acquisition/details/' + item.id]) }
        ];
        return colDef;
    }

    getSubRowDefs() {
        return [
            { headerName: "Id", field: "id", type: "number", width: "60px", defaultSortCol: true, defaultAsc: false },
            { headerName: "Name", field: "name", orderBy: ["updatedMetadata.name", "originMetadata.name", "id"] },
            { headerName: "Comment", field: "originMetadata.comment" },
            { headerName: "", type: "button", awesome: "fa-regular fa-eye", action: item => this.router.navigate(['/dataset/details/' + item.id]) }
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

    onSelectionChange() {
        this.nbSelectedDatasets = 0;
        this.datasetAcquisitions.forEach(acq => {
            if (this.selectedAcquisitionIds.has(acq.id)) {
                this.nbSelectedDatasets += acq.datasets?.length;
            }
        });
        this.updateOptions();
    }

    isCompatible(equipmentId: number): Boolean {
        if (this.studycard) {
            return this.studycard.acquisitionEquipment?.id == equipmentId;
        } else {
            return null;
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

    updateStudyCard() {
        this.updateNbIncompatible();
        this.breadcrumbsService.currentStep.data.studyCardId = this.studycard?.id;
    }

    updateOptions() {
        if (this.studyCards) {
            this.studycardOptions = [];
            this.studyCards.forEach(sc => {
                if (sc) {
                    let option: Option<StudyCard> = new Option(sc, sc.name);
                    option.compatible = this.datasetAcquisitions.findIndex(acq => acq.acquisitionEquipment?.id != sc.acquisitionEquipment?.id) == -1;
                    if (option.compatible || this.showIncompatibles) {
                        this.studycardOptions.push(option);
                    }
                }
            });
            this.updateNbIncompatible();
        }
    }

    updateShowIncompatible() {
        this.breadcrumbsService.currentStep.data.showIncompatibles = this.showIncompatibles;
        this.updateOptions();
    }

}
