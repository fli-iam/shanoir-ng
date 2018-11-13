import { Component,  Input, Output,  EventEmitter  } from '@angular/core';
import {  ActivatedRoute} from '@angular/router';
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
        private enumUtils: EnumUtils) 
    {
        super(route, 'preclinical-examination-anesthetics');
    }

    get examinationAnesthetic(): ExaminationAnesthetic { return this.entity; }
    set examinationAnesthetic(examinationAnesthetic: ExaminationAnesthetic) { this.entity = examinationAnesthetic; }

    

    initView(): Promise<void> {
        this.getEnums();
        this.loadData();
        return this.examAnestheticService.getExaminationAnesthetics(this.examination_id).then(examAnesthetics => {
            if (examAnesthetics && examAnesthetics.length > 0) {
                //Should be only one
                let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                this.examinationAnesthetic = examAnesthetic;
            }
        });
    }

    
    initEdit(): Promise<void> {
        this.getEnums();
        return this.loadData().then(() => {
            this.examAnestheticService.getExaminationAnesthetics(this.examination_id).then(examAnesthetics => {
                if (examAnesthetics && examAnesthetics.length > 0) {
                    //Should be only one
                    let examAnesthetic: ExaminationAnesthetic = examAnesthetics[0];
                    this.examinationAnesthetic = examAnesthetic;
                    this.examinationAnesthetic.dose_unit = this.getReferenceById(examAnesthetic.dose_unit);
                    this.examinationAnesthetic.anesthetic = this.getAnestheticById(examAnesthetic.anesthetic);
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
        this.anestheticService.getAnesthetics().then(anesthetics => {
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
        this.router.navigate(['/preclinical-anesthetic'], { queryParams: { mode: "create"} });
    }



}