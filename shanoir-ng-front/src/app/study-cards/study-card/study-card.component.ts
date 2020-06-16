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
import { FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';
import { AcquisitionEquipmentService } from '../../acquisition-equipments/shared/acquisition-equipment.service';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { CenterService } from '../../centers/shared/center.service';
import { NiftiConverterService } from '../../niftiConverters/nifti.converter.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { IdName } from '../../shared/models/id-name.model';
import { Option } from '../../shared/select/select.component';
import { StudyService } from '../../studies/shared/study.service';
import { StudyCard, StudyCardRule } from '../shared/study-card.model';
import { StudyCardService } from '../shared/study-card.service';
import { StudyCardRulesComponent } from '../study-card-rules/study-card-rules.component';

@Component({
    selector: 'study-card',
    templateUrl: 'study-card.component.html',
    styleUrls: ['study-card.component.css']
})
export class StudyCardComponent extends EntityComponent<StudyCard> {

    private centers: IdName[] = [];
    private studies: IdName[] = [];
    private acquisitionEquipments: Option<AcquisitionEquipment>[];
    private niftiConverters: IdName[] = [];
    showRulesErrors: boolean = false;
    selectMode: boolean;
    selectedRules: StudyCardRule[] = [];
    @ViewChild(StudyCardRulesComponent) rulesComponent: StudyCardRulesComponent;

    constructor(
            private route: ActivatedRoute,
            private studyCardService: StudyCardService, 
            private centerService: CenterService,
            private studyService: StudyService,
            private acqEqService: AcquisitionEquipmentService,
            private niftiConverterService: NiftiConverterService,
            private acqEqptLabelPipe: AcquisitionEquipmentPipe) {
        super(route, 'study-card');

        this.mode = this.activatedRoute.snapshot.data['mode'];
        this.selectMode = this.mode == 'view' && this.activatedRoute.snapshot.data['select'];
    }

    get studyCard(): StudyCard { return this.entity; }
    set studyCard(coil: StudyCard) { this.entity = coil; }

    initView(): Promise<void> {
        return this.studyCardService.get(this.id).then(sc => {
            this.studyCard = sc;
        });
    }
    
    initEdit(): Promise<void> {
        this.fetchStudies();
        this.fetchNiftiConverters();
        return this.studyCardService.get(this.id).then(sc => {
            this.studyCard = sc;
        });
    }

    initCreate(): Promise<void> {
        this.fetchStudies();
        this.fetchNiftiConverters();
        this.studyCard = new StudyCard();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        let form: FormGroup = this.formBuilder.group({
            'name': [this.studyCard.name, [Validators.required, Validators.minLength(2)]],
            'study': [this.studyCard.study, [Validators.required]],
            'acquisitionEquipment': [this.studyCard.acquisitionEquipment, [Validators.required]],
            'niftiConverter': [this.studyCard.niftiConverter, [Validators.required]],
            'rules': [this.studyCard.rules, [Validators.required, StudyCardRulesComponent.validator]]
        });
        this.subscribtions.push(
            form.get('study').valueChanges.subscribe(study => this.onStudyChange(study, form))
        );
        return form;
    }

    public hasEditRight(): boolean {
        return !this.selectMode && this.keycloakService.isUserAdminOrExpert();
    }
    
    private fetchStudies() {
        this.studyService.getStudiesNames()
            .then(studies => this.studies = studies);
    }

    private fetchAcqEq(studyId: number): Promise<void> {
        return this.acqEqService.getAllByStudy(studyId)
            .then(acqEqs => {
                this.acquisitionEquipments = [];
                for (let acqEq of acqEqs) {
                    let option: Option<AcquisitionEquipment> = new Option(acqEq, this.acqEqptLabelPipe.transform(acqEq));
                    this.acquisitionEquipments.push(option);
                }
            });
    }

    private fetchNiftiConverters() {
        this.niftiConverterService.getAll()
            .then(converters => this.niftiConverters = converters.map(converter => new IdName(converter.id, converter.name)));
    }

    private onStudyChange(study: IdName, form: FormGroup) {
        if (study) {
            this.fetchAcqEq(study.id).then(() => {
                if (this.studyCard.acquisitionEquipment) {
                    let found = this.acquisitionEquipments.find(acqOpt => acqOpt.value.id == this.studyCard.acquisitionEquipment.id);
                    if (!found) this.studyCard.acquisitionEquipment = null;
                }
            });
            form.get('acquisitionEquipment').enable();
        } else {
            form.get('acquisitionEquipment').disable();
            this.studyCard.acquisitionEquipment = null;
            this.acquisitionEquipments = [];
        }
    }

    onShowErrors() {
        this.form.markAsDirty();
        this.showRulesErrors = !this.showRulesErrors;
    }

    importRules() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/study-card/select-rule/list/' + this.entity.id]).then(success => {
            this.breadcrumbsService.currentStep.label = 'Select study-card';
            this.subscribtions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe((rules: StudyCardRule[]) => {
                    rules.forEach(rule => {
                        this.studyCard.rules.push(rule);
                        let lastIndex: number = this.studyCard.rules.length - 1;
                        currentStep.data.rulesToAnimate.add(lastIndex);
                    });
                })
            );
        });
    }

    clickImportRules() {
        this.breadcrumbsService.currentStep.notifySave(this.selectedRules);
        this.breadcrumbsService.goBack(2);
    }

    onChangeAcqEq() {
        this.rulesComponent.ruleElements.forEach(ruleComp => {
            ruleComp.assignmentChildren.forEach(assComp => {
                if (assComp.assignment.field.toLowerCase().includes('coil')) {
                    assComp.assignment.value = null
                    assComp.valueTouched = true;
                }
            })
        })
        this.form.get('rules').updateValueAndValidity();
    }

}