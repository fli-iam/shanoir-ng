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
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { Coil } from '../../coils/shared/coil.model';
import { CoilService } from '../../coils/shared/coil.service';
import { slideDown } from '../../shared/animations/animations';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { QualityCard } from '../shared/quality-card.model';
import { QualityCardService } from '../shared/quality-card.service';
import { StudyCardRule } from '../shared/study-card.model';
import { StudyCardRulesComponent } from '../study-card-rules/study-card-rules.component';

@Component({
    selector: 'quality-card',
    templateUrl: 'quality-card.component.html',
    styleUrls: ['quality-card.component.css'],
    animations: [slideDown]
})
export class QualityCardComponent extends EntityComponent<QualityCard> {

    centers: IdName[] = [];
    public studies: IdName[] = [];
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
            private qualityCardService: QualityCardService, 
            private studyService: StudyService,
            private studyRightsService: StudyRightsService,
            keycloakService: KeycloakService,
            coilService: CoilService) {
        super(route, 'quality-card');

        this.mode = this.activatedRoute.snapshot.data['mode'];
        this.selectMode = this.mode == 'view' && this.activatedRoute.snapshot.data['select'];
        this.isAdminOrExpert = keycloakService.isUserAdminOrExpert();
        coilService.getAll().then(coils => this.allCoils = coils);
     }

    getService(): EntityService<QualityCard> {
        return this.qualityCardService;
    }

    get qualityCard(): QualityCard { return this.entity; }
    set qualityCard(qc: QualityCard) { this.entity = qc; }

    initView(): Promise<void> {
        let scFetchPromise: Promise<void> = this.qualityCardService.get(this.id).then(sc => {
            this.qualityCard = sc;
        });
        this.hasAdministrateRightPromise = scFetchPromise.then(() => this.hasAdminRightsOnStudy());
        return scFetchPromise;
    }

    initEdit(): Promise<void> {
        this.hasAdministrateRightPromise = Promise.resolve(true);
        this.fetchStudies();
        return this.qualityCardService.get(this.id).then(sc => {
            this.qualityCard = sc;
        });
    }

    initCreate(): Promise<void> {
        this.hasAdministrateRightPromise = Promise.resolve(true);
        this.fetchStudies().then(() => {
            const studyId: number = parseInt(this.route.snapshot.paramMap.get('studyId'));
            if (studyId) {
                this.lockStudy = true;
                this.qualityCard.study = this.studies.find(st => st.id == studyId) as unknown as Study;
            }
        });
        this.qualityCard = new QualityCard();
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        let form: FormGroup = this.formBuilder.group({
            'name': [this.qualityCard.name, [Validators.required, Validators.minLength(2), this.registerOnSubmitValidator('unique', 'name')]],
            'study': [this.qualityCard.study, [Validators.required]],
            'toCheckAtImport': [this.qualityCard.toCheckAtImport, [Validators.required]],
            'rules': [this.qualityCard.rules, [StudyCardRulesComponent.validator]]
        });
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
            return this.studyRightsService.getMyRightsForStudy(this.qualityCard.study.id).then(rights => {
                return rights.includes(StudyUserRight.CAN_ADMINISTRATE);
            });
        }
    }
    
    private fetchStudies(): Promise<void | IdName[]> {
        return this.studyService.findStudyIdNamesIcanAdmin()
            .then(studies => this.studies = studies);
    }

    onShowErrors() {
        this.form.markAsDirty();
        this.showRulesErrors = !this.showRulesErrors;
    }

    // importRules() {
    //     let currentStep: Step = this.breadcrumbsService.currentStep;
    //     this.router.navigate(['/study-card/select-rule/list/' + this.entity.id]).then(success => {
    //         this.breadcrumbsService.currentStep.label = 'Select study-card';
    //         this.subscribtions.push(
    //             currentStep.waitFor(this.breadcrumbsService.currentStep).subscribe((rules: StudyCardRule[]) => {
    //                 rules.forEach(rule => {
    //                     this.studyCard.rules.push(rule);
    //                     let lastIndex: number = this.studyCard.rules.length - 1;
    //                     currentStep.data.rulesToAnimate.add(lastIndex);
    //                 });
    //             })
    //         );
    //     });
    // }

    // clickImportRules() {
    //     this.breadcrumbsService.currentStep.notifySave(this.selectedRules);
    //     this.breadcrumbsService.goBack(2);
    // }

    goToApply() {
        this.router.navigate(['/quality-card/apply/' + this.entity.id]);
    }
}
