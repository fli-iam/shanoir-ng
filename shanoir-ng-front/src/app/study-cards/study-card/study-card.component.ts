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
import { FormArray, FormGroup, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { AcquisitionEquipmentPipe } from '../../acquisition-equipments/shared/acquisition-equipment.pipe';
import { AcquisitionEquipmentService } from '../../acquisition-equipments/shared/acquisition-equipment.service';
import { Step } from '../../breadcrumbs/breadcrumbs.service';
import { CenterService } from '../../centers/shared/center.service';
import { Coil } from '../../coils/shared/coil.model';
import { CoilService } from '../../coils/shared/coil.service';
import { slideDown } from '../../shared/animations/animations';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { Option } from '../../shared/select/select.component';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { StudyCard, StudyCardRule } from '../shared/study-card.model';
import { StudyCardService } from '../shared/study-card.service';
import { StudyCardRuleComponent } from '../study-card-rules/study-card-rule.component';
import { StudyCardRulesComponent } from '../study-card-rules/study-card-rules.component';
import { Selection } from 'src/app/studies/study/tree.service';
import { DUAAssistantComponent } from 'src/app/dua/dua-assistant.component';

@Component({
    selector: 'study-card',
    templateUrl: 'study-card.component.html',
    styleUrls: ['study-card.component.css'],
    animations: [slideDown],
    standalone: false
})
export class StudyCardComponent extends EntityComponent<StudyCard> {

    centers: IdName[] = [];
    public studies: IdName[] = [];
    public acquisitionEquipments: Option<AcquisitionEquipment>[];
    public niftiConverters: IdName[] = [];
    showRulesErrors: boolean = false;
    selectMode: boolean;
    selectedRules: StudyCardRule[] = [];
    hasAdministrateRightPromise: Promise<boolean>;
    lockStudy: boolean = false;
    @ViewChild(StudyCardRulesComponent) rulesComponent: StudyCardRulesComponent;
    isAdminOrExpert: boolean;
    allCoils: Coil[];

    constructor(
            private route: ActivatedRoute,
            private studyCardService: StudyCardService,
            private studyService: StudyService,
            private acqEqService: AcquisitionEquipmentService,
            private studyRightsService: StudyRightsService,
            private acqEqptLabelPipe: AcquisitionEquipmentPipe,
            keycloakService: KeycloakService,
            private centerService: CenterService,
            coilService: CoilService) {
        super(route, 'study-card');
        this.mode = this.activatedRoute.snapshot.data['mode'];
        this.selectMode = this.mode == 'view' && this.activatedRoute.snapshot.data['select'];
        this.isAdminOrExpert = keycloakService.isUserAdminOrExpert();
        coilService.getAll().then(coils => this.allCoils = coils);
        this.subscriptions.push(this.onSave.subscribe(() => {
            let studyIdforDUA: number = this.breadcrumbsService.currentStep.data.goDUA;
            if (studyIdforDUA) {
                this.breadcrumbsService.currentStep.data.goDUA = undefined;
                DUAAssistantComponent.openCreateDialog(studyIdforDUA, this.confirmDialogService, this.router);
            }
        }));
     }

    getService(): EntityService<StudyCard> {
        return this.studyCardService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromStudycard(this.studyCard);
    }

    get studyCard(): StudyCard { return this.entity; }
    set studyCard(coil: StudyCard) { this.entity = coil; }

    initView(): Promise<void> {
        this.hasAdministrateRightPromise = this.hasAdminRightsOnStudy();
        return Promise.resolve();
    }

    initEdit(): Promise<void> {
        this.hasAdministrateRightPromise = Promise.resolve(false);
        this.fetchStudies();
        return Promise.resolve();
    }

    initCreate(): Promise<void> {
        this.hasAdministrateRightPromise = Promise.resolve(false);
        this.fetchStudies().then(() => {
            const studyId: number = parseInt(this.route.snapshot.paramMap.get('studyId'));
            if (studyId) {
                this.lockStudy = true;
                this.studyCard.study = this.studies.find(st => st.id == studyId) as unknown as Study;
            }
        });
        this.studyCard = new StudyCard();
        return Promise.resolve();
    }

    ngOnDestroy(): void {
        let studyIdforDUA: number = this.breadcrumbsService.currentStep.data.goDUA;
        if (studyIdforDUA) {
            this.breadcrumbsService.currentStep.data.goDUA = undefined;
            DUAAssistantComponent.openCreateDialog(studyIdforDUA, this.confirmDialogService, this.router);
        }
        super.ngOnDestroy();
    }

    buildForm(): UntypedFormGroup {
        let form: UntypedFormGroup = this.formBuilder.group({
            'name': [this.studyCard.name, [Validators.required, Validators.minLength(2), this.registerOnSubmitValidator('unique', 'name')]],
            'study': [this.studyCard.study, [Validators.required]],
            'acquisitionEquipment': [this.studyCard.acquisitionEquipment, [Validators.required]],
            'rules': [this.studyCard.rules, [StudyCardRulesComponent.validator]],
            'conditions': new FormArray([]),
        });
        this.subscriptions.push(
            form.get('study').valueChanges.subscribe(study => this.onStudyChange(study, form))
        );
        return form;
    }

    public async hasEditRight(): Promise<boolean> {
        return this.hasAdministrateRightPromise.then(hasRight => hasRight && !this.selectMode && this.keycloakService.isUserAdminOrExpert());
    }

    public async hasDeleteRight(): Promise<boolean> {
        return this.hasEditRight();
    }

    private hasAdminRightsOnStudy(): Promise<boolean> {
        if (this.keycloakService.isUserAdmin()) {
            return Promise.resolve(true);
        } else {
            return this.studyRightsService.getMyRightsForStudy(this.studyCard.study.id).then(rights => {
                return rights.includes(StudyUserRight.CAN_ADMINISTRATE);
            });
        }
    }

    private fetchStudies(): Promise<void | IdName[]> {
        return this.studyService.findStudyIdNamesIcanAdmin()
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

    private onStudyChange(study: IdName, form: UntypedFormGroup) {
        if (study) {
            this.fetchAcqEq(study.id).then(() => {
                if (this.studyCard.acquisitionEquipment) {
                    let found = this.acquisitionEquipments.find(acqOpt => acqOpt.value.id == this.studyCard.acquisitionEquipment.id);
                    if (!found) this.studyCard.acquisitionEquipment = null;
                }
            }).catch(err => {
                if (err.status != 404) throw err;
            });
            form.get('acquisitionEquipment').enable();
            this.centerService.getCentersNamesByStudyId(study.id).then(centers => {
                this.centers = centers;
                this.breadcrumbsService.currentStep.addPrefilled("center", this.centers);
            });
        } else {
            form.get('acquisitionEquipment').disable();
            this.studyCard.acquisitionEquipment = null;
            this.acquisitionEquipments = [];
        }
    }

    onShowErrors() {
        this.markControlsDirty(this.form);
        this.showRulesErrors = !this.showRulesErrors;
    }

    private markControlsDirty(group: FormGroup | FormArray): void {
        Object.keys(group.controls).forEach((key: string) => {
            const abstractControl = group.controls[key];
            if (abstractControl instanceof FormGroup || abstractControl instanceof FormArray) {
                this.markControlsDirty(abstractControl);
            } else {
                abstractControl.markAsDirty();
            }
        });
    }

    addConditionForm(form: FormGroup): FormGroup {
        if (this.mode != 'view') {
            setTimeout(() => { // prevent "changed after check" error
                (this.form.get('conditions') as FormArray).push(form);
            });
        }
        return this.form;
    }

    importRules() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/study-card/select-rule/list/' + this.entity.id]).then(success => {
            this.breadcrumbsService.currentStep.label = 'Select study-card';
            this.subscriptions.push(
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
        if (!this.rulesComponent) return;
        this.rulesComponent.ruleElements.forEach(ruleComp => {
            (ruleComp as StudyCardRuleComponent).assignmentChildren.forEach(assComp => {
                if (assComp.assignment.field.toLowerCase().includes('coil')) {
                    assComp.assignment.value = null
                    assComp.valueTouched = true;
                }
            })
        })
        this.form.get('rules').updateValueAndValidity();
    }

    goToApply() {
        this.router.navigate(['/study-card/apply/' + this.entity.id]);
    }

    createAcqEq() {
        let currentStep: Step = this.breadcrumbsService.currentStep;
        this.router.navigate(['/acquisition-equipment/create']).then(success => {
            this.breadcrumbsService.currentStep.addPrefilled("sc_center", this.centers);
            if (this.centers.length == 1) {
                this.breadcrumbsService.currentStep.addPrefilled('center', this.centers[0]);
            }
            this.subscriptions.push(
                currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe(entity => {
                    this.entity.acquisitionEquipment = entity as AcquisitionEquipment;
                })
            );
        });
    }

}
