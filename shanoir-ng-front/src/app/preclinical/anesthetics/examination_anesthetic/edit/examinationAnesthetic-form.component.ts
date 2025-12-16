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

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { UntypedFormGroup } from '@angular/forms';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';
import { InjectionInterval } from 'src/app/preclinical/shared/enum/injectionInterval';
import { InjectionSite } from 'src/app/preclinical/shared/enum/injectionSite';
import { InjectionType } from 'src/app/preclinical/shared/enum/injectionType';

import { ExaminationAnesthetic }    from '../shared/examinationAnesthetic.model';
import { ExaminationAnestheticService } from '../shared/examinationAnesthetic.service';
import { Reference }   from '../../../reference/shared/reference.model';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { Anesthetic }   from '../../anesthetic/shared/anesthetic.model';
import { AnestheticService } from '../../anesthetic/shared/anesthetic.service';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';

@Component({
    selector: 'examination-anesthetic-form',
    templateUrl: 'examinationAnesthetic-form.component.html',
    standalone: false
})
export class ExaminationAnestheticFormComponent extends EntityComponent<ExaminationAnesthetic> {
    @Input() isStandalone: boolean = false;
    @Input() form: UntypedFormGroup;
    @Output() examAnestheticChange = new EventEmitter();

    anesthetics: Anesthetic[] = [];
    sites: InjectionSite[] = [];
    intervals: InjectionInterval[] = [];
    injtypes: InjectionType[] = [];
    units: Reference[] = [];
    references: Reference[] = [];
    selectedStartDate: string = null;
    selectedEndDate: string = null;

    constructor(
        private route: ActivatedRoute,
        private examAnestheticService: ExaminationAnestheticService,
        private referenceService: ReferenceService,
        private anestheticService: AnestheticService
    ) {
        super(route, 'preclinical-examination-anesthetics');
    }

    get examinationAnesthetic(): ExaminationAnesthetic { return this.entity; }
    set examinationAnesthetic(examinationAnesthetic: ExaminationAnesthetic) { this.entity = examinationAnesthetic; }

    getService(): EntityService<ExaminationAnesthetic> {
        return this.examAnestheticService;
    }

    protected fetchEntity: () => Promise<ExaminationAnesthetic> = () => {
        return this.examAnestheticService.getExaminationAnesthetics(this.id).then(examAnesthetics => {
            if (examAnesthetics && examAnesthetics.length > 0) {
                //Should be only one
                const examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                examAnesthetic.internalId = examAnesthetic.id;
                return examAnesthetic;
            }
        });
    }

    initView(): Promise<void> {
        this.getEnums();
        this.examinationAnesthetic = new ExaminationAnesthetic();

        return this.loadAllData().then(() => {
            if (this.id)
                return this.loadExistingAnesthetic();
        });
    }


    initEdit(): Promise<void> {
        this.getEnums();

        return this.loadAllData().then(() => {
            if (!this.examinationAnesthetic)
                this.examinationAnesthetic = new ExaminationAnesthetic();

            if (this.examinationAnesthetic.doseUnit)
                this.examinationAnesthetic.doseUnit = this.findReferenceById(this.examinationAnesthetic.doseUnit);

            if (this.examinationAnesthetic.anesthetic)
                this.examinationAnesthetic.anesthetic = this.findAnestheticById(this.examinationAnesthetic.anesthetic);
        });
    }

    initCreate(): Promise<void> {
        this.getEnums();
        this.loadAllData();
        this.examinationAnesthetic = new ExaminationAnesthetic();
        this.examinationAnesthetic.examinationId = this.id;
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        const form: UntypedFormGroup = this.formBuilder.group({
            'anesthetic': [this.examinationAnesthetic.anesthetic],
            'injectionInterval': [this.examinationAnesthetic.injectionInterval],
            'injectionSite': [this.examinationAnesthetic.injectionSite],
            'injectionType': [this.examinationAnesthetic.injectionType],
            'dose': [this.examinationAnesthetic.dose],
            'doseUnit': [this.examinationAnesthetic.doseUnit],
            'startDate': [this.examinationAnesthetic.startDate],
            'endDate': [this.examinationAnesthetic.endDate]
        });
        this.subscriptions.push(
            form.valueChanges.subscribe(() => {
                this.eaChange();
            })
        );
        return form;
    }

    private loadAllData(): Promise<void> {
        const loadUnits = this.loadUnits();
        const loadAnesthetics = this.loadAnesthetics();

        return Promise.all([loadUnits, loadAnesthetics]).then(() => {
            // Les deux sont maintenant chargées, on peut configurer references
            this.references = this.units || [];
        });
    }

    private loadUnits(): Promise<void> {
        return this.referenceService
            .getReferencesByCategoryAndType(
                PreclinicalUtils.PRECLINICAL_CAT_UNIT,
                PreclinicalUtils.PRECLINICAL_UNIT_VOLUME
            )
            .then(units => {
                this.units = units || [];
            })
            .catch(error => {
                console.error('Error loading units:', error);
                this.units = [];
            });
    }

    private loadAnesthetics(): Promise<void> {
        return this.anestheticService
            .getAll()
            .then(anesthetics => {
                this.anesthetics = anesthetics || [];
            })
            .catch(error => {
                console.error('Error loading anesthetics:', error);
                this.anesthetics = [];
            });
    }

    private loadExistingAnesthetic(): Promise<void> {
        return this.fetchEntity().then(entity => {
            if (entity) {
                this.examinationAnesthetic = entity;
            }
        });
    }

    private findReferenceById(reference: any): Reference {
        if (!reference || !this.references) return null;

        const refId = typeof reference === 'object' ? reference.id : reference;
        return this.references.find(ref => ref.id === refId) || null;
    }

    private findAnestheticById(anesthetic: any): Anesthetic {
        if (!anesthetic || !this.anesthetics) return null;

        const anestheticId = typeof anesthetic === 'object' ? anesthetic.id : anesthetic;
        return this.anesthetics.find(anest => anest.id === anestheticId) || null;
    }

    getEnums(): void {
        this.intervals = InjectionInterval.all();
        this.sites = InjectionSite.all();
        this.injtypes = InjectionType.all();
    }

    eaChange() {
        if(!this.isStandalone && this.examinationAnesthetic) {
            this.examAnestheticChange.emit(this.examinationAnesthetic);
        }
    }

    goToAddAnesthetic() {
        this.router.navigate(['/preclinical-anesthetic/create']);
    }

    public save(): Promise<ExaminationAnesthetic> {
        if (this.examinationAnesthetic.id) {
            return this.updateExaminationAnesthetic().then(exam => {
                this.onSave.next(this.examinationAnesthetic);
                this.chooseRouteAfterSave(this.entity);
                this.consoleService.log('info', 'Preclinical examination n°' + this.examinationAnesthetic.id + ' successfully updated');
                return exam;
            });
        } else {
            return this.addExaminationAnesthetic().then(exam => {
                this.onSave.next(this.examinationAnesthetic);
                this.chooseRouteAfterSave(this.entity);
                this.consoleService.log('info', 'New preclinical examination successfully saved with n°' + this.examinationAnesthetic.id);
                return exam;
            });
        }
    }

    addExaminationAnesthetic(): Promise<ExaminationAnesthetic> {
        if (!this.examinationAnesthetic) {
            return Promise.resolve(null);
        } else {
            return Promise.resolve(this.examAnestheticService.createAnesthetic(this.examinationAnesthetic.examinationId, this.examinationAnesthetic));
        }
    }

    updateExaminationAnesthetic(): Promise<ExaminationAnesthetic> {
        return this.examAnestheticService.update(this.examinationAnesthetic.examinationId, this.examinationAnesthetic).then(() => this.examinationAnesthetic);
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }

}
