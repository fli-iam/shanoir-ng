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
import { FormGroup,  Validators } from '@angular/forms';
import {  ActivatedRoute } from '@angular/router';

import { ContrastAgent }    from '../shared/contrastAgent.model';
import { ContrastAgentService } from '../shared/contrastAgent.service';
import { Reference }   from '../../reference/shared/reference.model';
import { ReferenceService } from '../../reference/shared/reference.service';
import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { Enum } from "../../../shared/utils/enum";
import { EnumUtils } from "../../shared/enum/enumUtils";
import { slideDown } from '../../../shared/animations/animations';
import { ModesAware } from "../../shared/mode/mode.decorator";
import { EntityComponent } from '../../../shared/components/entity/entity.component.abstract';
import { EntityService } from 'src/app/shared/components/entity/entity.abstract.service';

@Component({
    selector: 'contrast-agent-form',
    templateUrl: 'contrastAgent-form.component.html',
    styleUrls: ['contrastAgent-form.component.css'],
    providers: [ContrastAgentService, ReferenceService],
    animations: [slideDown]
})
@ModesAware
export class ContrastAgentFormComponent extends EntityComponent<ContrastAgent>{

    @Input() protocol_id: number;
    @Output() closing = new EventEmitter();
    @Output() agentChange = new EventEmitter();
    @Input() canModify: Boolean = false;
    @Input() isStandalone: boolean = true;
    agentNames: Reference[] = [];
    sites: Enum[] = [];
    intervals: Enum[] = [];
    injtypes: Enum[] = [];
    dose_units: Reference[] = [];
    concentration_units: Reference[] = [];
    references: Reference[] = [];

    constructor(
        private route: ActivatedRoute,
        private contrastAgentsService: ContrastAgentService,
        private referenceService: ReferenceService, 
        public enumUtils: EnumUtils) {

        super(route, 'preclinical-contrast-agent');
    }

    get agent(): ContrastAgent { return this.entity; }
    set agent(agent: ContrastAgent) { this.entity = agent; }

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
                agent.dose_unit = this.getReferenceById(agent.dose_unit);
                agent.concentration_unit = this.getReferenceById(agent.concentration_unit);
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
                agent.dose_unit = this.getReferenceById(agent.dose_unit);
                agent.concentration_unit = this.getReferenceById(agent.concentration_unit);
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

    buildForm(): FormGroup {
        return this.formBuilder.group({
            'name': [this.agent.name, Validators.required],
            'manufactured_name': [this.agent.manufactured_name],
            'dose': [this.agent.dose],
            'dose_unit': [this.agent.dose_unit],
            'concentration': [this.agent.concentration],
            'concentration_unit': [this.agent.concentration_unit],
            'injectionInterval': [this.agent.injection_interval],
            'injectionSite': [this.agent.injection_site],
            'injectionType': [this.agent.injection_type],
        });
    }

    loadReferences() {
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_CONTRAST_AGENT, PreclinicalUtils.PRECLINICAL_CONTRAST_AGENT_NAME).then(names => {
            this.agentNames = names;
        });
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT, PreclinicalUtils.PRECLINICAL_UNIT_VOLUME).then(units => {
            this.dose_units = units;
        });
        this.referenceService.getReferencesByCategoryAndType(PreclinicalUtils.PRECLINICAL_CAT_UNIT, PreclinicalUtils.PRECLINICAL_UNIT_CONCENTRATION).then(units => {
            this.concentration_units = units;
            this.references = this.agentNames.concat(this.dose_units.concat(this.concentration_units));
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
        this.intervals = this.enumUtils.getEnumArrayFor('InjectionInterval');
        this.sites = this.enumUtils.getEnumArrayFor('InjectionSite');
        this.injtypes = this.enumUtils.getEnumArrayFor('InjectionType');
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
            for (let ref of this.references) {
                if (reference.id == ref.id) {
                    return ref;
                }
            }
        }
        return null;
    }

}