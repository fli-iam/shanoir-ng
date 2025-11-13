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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UntypedFormGroup, Validators, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

import { slideDown } from '../../../shared/animations/animations';
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';
import { Reference } from '../../reference/shared/reference.model';
import { ReferenceService } from '../../reference/shared/reference.service';
import { InjectionInterval } from '../../shared/enum/injectionInterval';
import { InjectionSite } from '../../shared/enum/injectionSite';
import { InjectionType } from '../../shared/enum/injectionType';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { ContrastAgent } from '../shared/contrastAgent.model';
import { ContrastAgentService } from '../shared/contrastAgent.service';

import { FormFooterComponent } from '../../../shared/components/form-footer/form-footer.component';

@Component({
    selector: 'contrast-agent-form',
    templateUrl: 'contrastAgent-form.component.html',
    styleUrls: ['contrastAgent-form.component.css'],
    animations: [slideDown],
    imports: [FormsModule, ReactiveFormsModule, FormFooterComponent]
})
export class ContrastAgentFormComponent extends EntityComponent<ContrastAgent>{

    @Input() protocol_id: number;
    @Output() closing = new EventEmitter();
    @Output() agentChange = new EventEmitter();
    @Input() canModify: boolean = false;
    @Input() isStandalone: boolean = true;
    agentNames: Reference[] = [];
    sites: InjectionSite[] = [];
    intervals: InjectionInterval[] = [];
    injtypes: InjectionType[] = [];
    doseUnits: Reference[] = [];
    concentration_units: Reference[] = [];
    references: Reference[] = [];

    constructor(
        private route: ActivatedRoute,
        private contrastAgentsService: ContrastAgentService,
        private referenceService: ReferenceService) {

        super(route, 'preclinical-contrast-agent');
    }

    get agent(): ContrastAgent { return this.entity; }
    set agent(agent: ContrastAgent) { this.entity = agent; }

    getService(): EntityService<ContrastAgent> {
        return this.contrastAgentsService;
    }

    initView(): Promise<void> {
        this.entity = new ContrastAgent();
        this.getEnums();
        this.loadReferences();
        return this.contrastAgentsService.getContrastAgent(this.protocol_id).then(agent => {
            if (agent) {
                agent.name = this.getReferenceById(agent.name);
                agent.doseUnit = this.getReferenceById(agent.doseUnit);
                agent.concentrationUnit = this.getReferenceById(agent.concentrationUnit);
                this.agent = agent;
            }  
        });
    }

    initEdit(): Promise<void> {
        this.entity = new ContrastAgent();
        this.getEnums();
        this.loadReferences();
        return this.contrastAgentsService.getContrastAgent(this.protocol_id).then(agent => {
            if (agent) {
                agent.name = this.getReferenceById(agent.name);
                agent.doseUnit = this.getReferenceById(agent.doseUnit);
                agent.concentrationUnit = this.getReferenceById(agent.concentrationUnit);
                this.agent = agent;
            }  
        });
    }

    initCreate(): Promise<void> {
        this.entity = new ContrastAgent();
        this.getEnums();
        this.loadReferences();
        return Promise.resolve();
    }

    buildForm(): UntypedFormGroup {
        const form: UntypedFormGroup = this.formBuilder.group({
            'name': [this.agent.name, Validators.required],
            'manufacturedName': [this.agent.manufacturedName],
            'dose': [this.agent.dose],
            'doseUnit': [this.agent.doseUnit],
            'concentration': [this.agent.concentration],
            'concentrationUnit': [this.agent.concentrationUnit],
            'injectionInterval': [this.agent.injectionInterval],
            'injectionSite': [this.agent.injectionSite],
            'injectionType': [this.agent.injectionType],
        });
        this.subscriptions.push(
            form.valueChanges.subscribe(() => {
                this.onAgentChange();
            })
        );
        return form;
    }

    loadReferences() {
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_CONTRAST_AGENT, PreclinicalUtils.PRECLINICAL_CONTRAST_AGENT_NAME).then(names => {
            this.agentNames = names;
        });
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT, PreclinicalUtils.PRECLINICAL_UNIT_VOLUME).then(units => {
            this.doseUnits = units;
        });
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT, PreclinicalUtils.PRECLINICAL_UNIT_CONCENTRATION).then(units => {
            this.concentration_units = units;
            this.references = this.agentNames.concat(this.doseUnits.concat(this.concentration_units));
        });
    }


    goToAddContrastAgent(){
        this.router.navigate(['/preclinical-reference/create'], { queryParams: { category:"contrastagent", reftype:"name" } });
    }

    
    onAgentChange(){
        if(!this.isStandalone) {
            this.agentChange.emit(this.agent);
        }        
    }

    getEnums(): void {
        this.intervals = InjectionInterval.all();
        this.sites = InjectionSite.all();
        this.injtypes = InjectionType.all();
    }


    addContrastAgent() {
        if (!this.agent) { return; }
        this.contrastAgentsService.createConstrastAgent(this.protocol_id, this.agent);
    }

    updateContrastAgent(): void {
        this.contrastAgentsService.update(this.protocol_id, this.agent);
    }

    getReferenceById(reference: any): Reference {
        if (reference) {
            for (const ref of this.references) {
                if (reference.id == ref.id) {
                    return ref;
                }
            }
        }
        return null;
    }

    public async hasDeleteRight(): Promise<boolean> {
        return false;
    }

}