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

import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { StudyCard } from '../shared/study-card.model';
import { StudyCardListComponent } from './study-card-list.component';
import { StudyCardService } from '../shared/study-card.service';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';
import { ActivatedRoute } from '@angular/router';



@Component({
    selector: 'study-card-list-for-rules',
    templateUrl: 'study-card-list-for-rules.component.html',
    styleUrls: ['study-card-list.component.css'],
})
export class StudyCardForRulesListComponent extends StudyCardListComponent implements OnInit {

    private idToExclude: number;

    constructor(
            studyCardService: StudyCardService,
            acqEqptLabelPipe: AcquisitionEquipmentPipe,
            private activatedRoute: ActivatedRoute) {
            
        super(studyCardService, acqEqptLabelPipe);
        this.breadcrumbsService.resetMilestone();   
    }

    ngOnInit() {
        super.ngOnInit();
        this.idToExclude = +this.activatedRoute.snapshot.params['id'];
    }

    getColumnDefs(): any[] {
        let colDef: any[] = [
            { headerName: "Name", field: "name" },
            { headerName: "Study", field: 'study.name', defaultField: 'study.id' },
            { headerName: "Center", field: 'acquisitionEquipment.center.name'},
            { headerName: "Equipment", field: "acquisitionEquipment", width: '200%',
                cellRenderer: params => this.format(params.data.acquisitionEquipment) },
            { headerName: "Nb of rules", type: 'number', field: "rules.length", width: '30px' ,
                cellRenderer: params => params.data.rules ? params.data.rules.length+'' : '0' }
        ];
        return colDef;       
    }

    disableCondition(studycard: StudyCard): boolean {
        return studycard.id == this.idToExclude || !studycard.rules || studycard.rules.length == 0; 
    }

    getOptions() {
        return {
            new: false,
            view: false, 
            edit: false, 
            delete: false
        };
    }  

    onRowClick(sc: StudyCard) {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/study-card/select-rule/select/' + sc.id]).then(success => {
            this.breadcrumbsService.currentStep.label = 'Import rule';
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                    currentStep.notifySave(entity);
                })
            );
        });
    }
}