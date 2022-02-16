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

import { Component,  Input, Output,  EventEmitter  } from '@angular/core';
import {  ActivatedRoute, ResolveEnd} from '@angular/router';
import { FormGroup } from '@angular/forms';

import { ExaminationAnesthetic }    from '../shared/examinationAnesthetic.model';
import { ExaminationAnestheticService } from '../shared/examinationAnesthetic.service';
import { Reference }   from '../../../reference/shared/reference.model';
import { ReferenceService } from '../../../reference/shared/reference.service';
import { Anesthetic }   from '../../anesthetic/shared/anesthetic.model';
import { AnestheticService } from '../../anesthetic/shared/anesthetic.service';
import * as PreclinicalUtils from '../../../utils/preclinical.utils';
import { Enum } from "../../../../shared/utils/enum";
import { EnumUtils } from "../../../shared/enum/enumUtils";
import { ModesAware } from "../../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../../shared/components/entity/entity.component.abstract';
import { MsgBoxService } from '../../../../shared/msg-box/msg-box.service';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

@Component({
    selector: 'examination-anesthetic-form',
    templateUrl: 'examinationAnesthetic-form.component.html',
    providers: [ExaminationAnestheticService, ReferenceService, AnestheticService]
})
@ModesAware
export class ExaminationAnestheticFormComponent extends EntityComponent<ExaminationAnesthetic> {

    @Input() isStandalone: boolean = false;
    @Input() examination_id: number;
    @Input() form: FormGroup;
    @Output() examAnestheticChange = new EventEmitter();
    //examinationAnesthetic : ExaminationAnesthetic;
    anesthetics: Anesthetic[] = [];
    sites: Enum[] = [];
    intervals: Enum[] = [];
    injtypes: Enum[] = [];
    units: Reference[] = [];
    references: Reference[] = [];
    selectedStartDate: string = null;
    selectedEndDate: string = null;

    constructor(
        private route: ActivatedRoute,
        private examAnestheticService: ExaminationAnestheticService,
        private referenceService: ReferenceService,
        private anestheticService: AnestheticService,
        public enumUtils: EnumUtils) 
    {
        super(route, 'preclinical-examination-anesthetics');
    }

    get examinationAnesthetic(): ExaminationAnesthetic { return this.entity; }
    set examinationAnesthetic(examinationAnesthetic: ExaminationAnesthetic) { this.entity = examinationAnesthetic; }

    getService(): EntityService<ExaminationAnesthetic> {
        return this.examAnestheticService;
    }    

    initView(): Promise<void> {
        this.getEnums();
        this.examinationAnesthetic = new ExaminationAnesthetic();
        return this.loadData().then(() => {
            this.examAnestheticService.getExaminationAnesthetics(this.examination_id).then(examAnesthetics => {
                if (examAnesthetics && examAnesthetics.length > 0) {
                    //Should be only one
                    let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                    this.examinationAnesthetic = examAnesthetic;
                    this.examinationAnesthetic.internal_id = examAnesthetic.id;
                }
            });
        });
    }

    
    initEdit(): Promise<void> {
        this.getEnums();
        this.examinationAnesthetic = new ExaminationAnesthetic();
        return this.loadData().then(() => {
            this.examAnestheticService.getExaminationAnesthetics(this.examination_id).then(examAnesthetics => {
                if (examAnesthetics && examAnesthetics.length > 0) {
                    //Should be only one
                    let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                    this.examinationAnesthetic = examAnesthetic;
                    this.examinationAnesthetic.dose_unit = this.getReferenceById(examAnesthetic.dose_unit);
                    this.examinationAnesthetic.anesthetic = this.getAnestheticById(examAnesthetic.anesthetic);
                    this.examinationAnesthetic.internal_id = examAnesthetic.id;
                }
            });
        });


    }

    initCreate(): Promise<void> {
        this.getEnums();
        this.loadData();
        this.examinationAnesthetic = new ExaminationAnesthetic();
        this.examinationAnesthetic.examination_id = this.examination_id;
        return Promise.resolve();
    }

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'anesthetic': [this.examinationAnesthetic.anesthetic],
            'injectionInterval': [this.examinationAnesthetic.injection_interval],
            'injectionSite': [this.examinationAnesthetic.injection_site],
            'injectionType': [this.examinationAnesthetic.injection_type],
            'dose': [this.examinationAnesthetic.dose],
            'dose_unit': [this.examinationAnesthetic.dose_unit],
            'startDate': [this.examinationAnesthetic.startDate],
            'endDate': [this.examinationAnesthetic.endDate]
        });
    }


    loadData(): Promise<void> {
        return this.loadReferences();
    }

    loadReferences(): Promise<void> {
        return this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT, PreclinicalUtils.PRECLINICAL_UNIT_VOLUME).then(units => {
            this.units = units;
            this.loadAnesthetics();
        });

    }

    loadAnesthetics() {
        this.anestheticService.getAll().then(anesthetics => {
            this.anesthetics = anesthetics;
            this.references = this.units;
        });
    }

    getEnums(): void {
        this.intervals = this.enumUtils.getEnumArrayFor('InjectionInterval');
        this.sites = this.enumUtils.getEnumArrayFor('InjectionSite');
        this.injtypes = this.enumUtils.getEnumArrayFor('InjectionType');
    }
    

    getReferenceById(reference: any): Reference {
        if (reference) {
            for (let ref of this.references) {
                if (reference.id == ref.id) {
                    return ref;
                }
            }
        }
        return null;
    }

    getAnestheticById(anesthetic: any): Anesthetic {
        if (anesthetic) {
            for (let anest of this.anesthetics) {
                if (anesthetic.id == anest.id) {
                    return anest;
                }
            }
        }
        return null;
    }
    
    
    eaChange(){
        if(!this.isStandalone && this.examinationAnesthetic) {
            this.examAnestheticChange.emit(this.examinationAnesthetic);
        }       
    }

    

    goToAddAnesthetic(){
        this.router.navigate(['/preclinical-anesthetic/create']);
    }

    public save(): Promise<ExaminationAnesthetic> {
        if (this.examinationAnesthetic.id) {
            return this.updateExaminationAnesthetic().then(exam => {
                this.onSave.next(this.examinationAnesthetic);
                this.chooseRouteAfterSave(this.entity);
                this.msgBoxService.log('info', 'The preclinical-examination n°' + this.examinationAnesthetic.id + ' has been successfully updated');
                return exam;
            });
        } else {
            return this.addExaminationAnesthetic().then(exam => {
                this.onSave.next(this.examinationAnesthetic);
                this.chooseRouteAfterSave(this.entity);
                this.msgBoxService.log('info', 'The new preclinical-examination has been successfully saved under the number ' + this.examinationAnesthetic.id);
                return exam;
            });
        }
    }

    addExaminationAnesthetic(): Promise<ExaminationAnesthetic> {
        if (!this.examinationAnesthetic) { 
            return Promise.resolve(null); 
        } else {
            return Promise.resolve(this.examAnestheticService.createAnesthetic(this.examinationAnesthetic.examination_id, this.examinationAnesthetic));
        }
    }

    updateExaminationAnesthetic(): Promise<ExaminationAnesthetic> {
        return this.examAnestheticService.update(this.examinationAnesthetic.examination_id, this.examinationAnesthetic).then(() => this.examinationAnesthetic);
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }

}