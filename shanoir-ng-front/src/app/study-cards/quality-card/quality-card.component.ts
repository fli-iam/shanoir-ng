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
import { Component, ComponentRef, EventEmitter, ViewChild } from '@angular/core';
import { FormArray, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Observable, race } from 'rxjs';
import { map, take } from 'rxjs/operators';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { ExaminationService } from 'src/app/examinations/shared/examination.service';
import { ServiceLocator } from 'src/app/utils/locator.service';
import { SuperPromise } from 'src/app/utils/super-promise';
import { Selection } from 'src/app/studies/study/tree.service';

import { Coil } from '../../coils/shared/coil.model';
import { CoilService } from '../../coils/shared/coil.service';
import { slideDown } from '../../shared/animations/animations';
import { ConfirmDialogService } from '../../shared/components/confirm-dialog/confirm-dialog.service';
import { EntityComponent } from '../../shared/components/entity/entity.component.abstract';
import { BrowserPaging } from '../../shared/components/table/browser-paging.model';
import { ColumnDefinition } from '../../shared/components/table/column.definition.type';
import { FilterablePageable, Page } from '../../shared/components/table/pageable.model';
import { TableComponent } from '../../shared/components/table/table.component';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { IdName } from '../../shared/models/id-name.model';
import { StudyRightsService } from '../../studies/shared/study-rights.service';
import { StudyUserRight } from '../../studies/shared/study-user-right.enum';
import { Study } from '../../studies/shared/study.model';
import { StudyService } from '../../studies/shared/study.service';
import { QualityCard } from '../shared/quality-card.model';
import { Interval, QualityCardService } from '../shared/quality-card.service';
import { StudyCardRule } from '../shared/study-card.model';
import { StudyCardRulesComponent } from '../study-card-rules/study-card-rules.component';
import * as AppUtils from '../../utils/app.utils';
import { TestQualityCardOptionsComponent } from '../test-quality-card-options/test-quality-card-options.component';


@Component({
    selector: 'quality-card',
    templateUrl: 'quality-card.component.html',
    styleUrls: ['quality-card.component.css'],
    animations: [slideDown],
    standalone: false
})
export class QualityCardComponent extends EntityComponent<QualityCard> {

    centers: IdName[] = [];
    public studies: IdName[] = [];
    showRulesErrors: boolean = false;
    selectMode: boolean;
    selectedRules: StudyCardRule[] = [];
    hasAdministrateRightPromise: Promise<boolean>;
    isStudyAdmin: boolean;
    lockStudy: boolean = false;
    @ViewChild(StudyCardRulesComponent) rulesComponent: StudyCardRulesComponent;
    isAdminOrExpert: boolean;
    allCoils: Coil[];
    applying: boolean = false;
    testing: boolean = false;
    report: BrowserPaging<any>;
    reportIsTest: boolean;
    reportColumns: ColumnDefinition[] = [
        {headerName: 'Subject Name', field: 'subjectName', width: '20%'},
        {headerName: 'Examination Comment', field: 'examinationComment', width: '25%'},
        {headerName: 'Examination Date', field: 'examinationDate', type: 'date', width: '100px'},
        {headerName: 'Details', field: 'message', wrap: true}
    ];
    forceStudyId: number;
    nbExaminations: number;
    progress: number;
    readonly NB_EXAM_THRESHOLD: number = 50;

    constructor(
            private route: ActivatedRoute,
            private qualityCardService: QualityCardService, 
            private studyService: StudyService,
            private examinationService: ExaminationService,
            private studyRightsService: StudyRightsService,
            keycloakService: KeycloakService,
            coilService: CoilService,
            private confirmService: ConfirmDialogService) {
        super(route, 'quality-card');

        this.mode = this.activatedRoute.snapshot.data['mode'];
        this.selectMode = this.mode == 'view' && this.activatedRoute.snapshot.data['select'];
        this.isAdminOrExpert = keycloakService.isUserAdminOrExpert();
        coilService.getAll().then(coils => this.allCoils = coils);

        this.subscriptions.push(this.activatedRoute.params.subscribe(
            params => {
                this.forceStudyId = +params['studyId'];
            }
        ));
     }

    getService(): EntityService<QualityCard> {
        return this.qualityCardService;
    }

    protected getTreeSelection: () => Selection = () => {
        return Selection.fromQualitycard(this.qualityCard);
    }

    get qualityCard(): QualityCard { return this.entity; }
    set qualityCard(qc: QualityCard) { this.entity = qc; }

    initView(): Promise<void> {
        this.hasAdministrateRightPromise = this.hasAdminRightsOnStudy().then(res => this.isStudyAdmin = res);
        return Promise.resolve();  
    }

    initEdit(): Promise<void> {
        this.hasAdministrateRightPromise = this.hasAdminRightsOnStudy().then(res => this.isStudyAdmin = res);
        this.fetchStudies();
        return Promise.resolve();  
    }

    initCreate(): Promise<void> {
        this.hasAdministrateRightPromise = Promise.resolve(false);
        this.fetchStudies().then(() => {
            if (this.forceStudyId) {
                this.lockStudy = true;
                this.qualityCard.study = this.studies.find(st => st.id == this.forceStudyId) as unknown as Study;
                this.onStudyChange();
            }
        });
        this.qualityCard = new QualityCard();
        return Promise.resolve();
    }

    onStudyChange() {
        this.hasAdministrateRightPromise = this.hasAdminRightsOnStudy().then(res => this.isStudyAdmin = res);
    }

    buildForm(): FormGroup {
        let form: FormGroup = this.formBuilder.group({
            'name': [this.qualityCard.name, [Validators.required, Validators.minLength(2), this.registerOnSubmitValidator('unique', 'name')]],
            'study': [this.qualityCard.study, [Validators.required]],
            'toCheckAtImport': [this.qualityCard.toCheckAtImport, [Validators.required]],
            'rules': [this.qualityCard.rules, [StudyCardRulesComponent.validator]],
            'conditions': new FormArray([]),
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

    apply() {
        this.confirmService.confirm(
            'Apply Quality Card', 
            `Do you want to apply the quality card named "${this.qualityCard.name}" all over the study "${this.qualityCard.study.name}" ? This would permanentely overwrite previous quality tags for the study's subjects.`
        ).then(accept => {
            if (accept) {
                this.applying = true;
                this.report = null;
                this.qualityCardService.applyOnStudy(this.qualityCard.id).then(result => {
                    this.report = new BrowserPaging(result, this.reportColumns);
                    this.reportIsTest = false;
                }).finally(() => this.applying = false);
            }
        });
    }

    test() {
        this.testing = true;
        this.report = null;

        this.examinationService.findExaminationIdsByStudy(this.qualityCard.study.id).then(examIds => {
            this.nbExaminations = examIds.length;
            if (examIds?.length > 0) {
                if (examIds.length > this.NB_EXAM_THRESHOLD) {
                    this.openSetTestInterval(this.nbExaminations).then(response => {
                        if (!response) {
                            this.performTest();
                        } else if (response instanceof Interval) {
                            this.performTest((response as Interval).from, (response as Interval).to);
                        }
                    }).finally(() => {
                        this.testing = false;
                    });
                } else {
                    this.performTest();
                }
            }
        });    
    }

    openSetTestInterval(nbExaminations: number): Promise<Interval | 'cancel'> {
        let modalRef: ComponentRef<TestQualityCardOptionsComponent> = ServiceLocator.rootViewContainerRef.createComponent(TestQualityCardOptionsComponent);
        modalRef.instance.nbExaminations = nbExaminations;
        return this.waitForEnd(modalRef);
    }

    private waitForEnd(modalRef: ComponentRef<{ test: EventEmitter<any>, closeModal: EventEmitter<void> }>): Promise<Interval | 'cancel'> {
        let resPromise: SuperPromise<any | 'cancel'> = new SuperPromise();
        let result: Observable<any> = race([
            modalRef.instance.test, 
            modalRef.instance.closeModal.pipe(map(() => 'cancel'))
        ]);
        result.pipe(take(1)).subscribe(ret => {
            modalRef.destroy();
            resPromise.resolve(ret);
        }, error => {
            modalRef.destroy();
            resPromise.reject(error);
        });
        return resPromise;
    }

    performTest(start?: number, stop?: number) {
        this.qualityCardService.testOnStudy(this.qualityCard.id, start, stop).then(result => {
            this.report = new BrowserPaging(result, this.reportColumns);
            this.reportIsTest = true;
        }).finally(() => this.testing = false);
    }

    getPage(pageable: FilterablePageable): Promise<Page<any>> {
        return Promise.resolve(this.report.getPage(pageable));
    }

    static downloadReport(report: {columnDefs: any[], items: any[]}, name?: string) {
        if (!report) return;
        let csvStr: string = '';
        csvStr += report.columnDefs.map(col => col.headerName).join(',');
        for (let entry of report.items) {
            csvStr += '\n' + report.columnDefs.map(col => '"' + TableComponent.getCellValue(entry, col) + '"').join(',');
        }
        const csvBlob = new Blob([csvStr], {
            type: 'text/csv'
        });
        AppUtils.browserDownloadFile(csvBlob, 'qcReport_' + (name ? name + '_' : '') + Date.now().toLocaleString('fr-FR'));
    }

    protected downloadReport() {
        QualityCardComponent.downloadReport(this.report, this.qualityCard?.name);
    }
}