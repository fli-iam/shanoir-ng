import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormGroup, FormBuilder, Validators, FormControl } from '@angular/forms';
import { Router, ActivatedRoute, Params } from '@angular/router';
import { Observable } from 'rxjs/Observable';
import { Location } from '@angular/common';

import { ContrastAgent }    from '../shared/contrastAgent.model';
import { ContrastAgentService } from '../shared/contrastAgent.service';
import { Reference }   from '../../reference/shared/reference.model';
import { ReferenceService } from '../../reference/shared/reference.service';

import * as PreclinicalUtils from '../../utils/preclinical.utils';
import { KeycloakService } from "../../../shared/keycloak/keycloak.service";

import { Enum } from "../../../shared/utils/enum";
import { InjectionType } from "../../shared/enum/injectionType";
import { InjectionInterval } from "../../shared/enum/injectionInterval";
import { InjectionSite } from "../../shared/enum/injectionSite";
import { EnumUtils } from "../../shared/enum/enumUtils";
import { Mode } from "../../shared/mode/mode.model";
import { Modes } from "../../shared/mode/mode.enum";
import { ModesAware } from "../../shared/mode/mode.decorator";
import { ImagesUrlUtil } from '../../../shared/utils/images-url.util';

@Component({
    selector: 'contrast-agent-form',
    templateUrl: 'contrastAgent-form.component.html',
    styleUrls: ['contrastAgent-form.component.css'],
    providers: [ContrastAgentService, ReferenceService]
})
@ModesAware
export class ContrastAgentFormComponent implements OnInit {
    newContrastAgentForm: FormGroup;
    @Input() protocol_id: number;
    @Input() agent: ContrastAgent = new ContrastAgent();
    @Output() closing = new EventEmitter();
    @Output() agentChange = new EventEmitter();
    @Input() mode: Mode = new Mode();
    @Input() canModify: Boolean = false;
    @Input() isStandalone: boolean = true;
    agentNames: Reference[] = [];
    sites: Enum[] = [];
    intervals: Enum[] = [];
    injtypes: Enum[] = [];
    dose_units: Reference[] = [];
    concentration_units: Reference[] = [];
    references: Reference[] = [];
    private addIconPath: string = ImagesUrlUtil.ADD_ICON_PATH;

    constructor(
        private contrastAgentsService: ContrastAgentService,
        private referenceService: ReferenceService,
        private keycloakService: KeycloakService,
        private enumUtils: EnumUtils,
        private fb: FormBuilder,
        private route: ActivatedRoute,
        private router: Router,
        private location: Location) {

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
            this.getContrastAgent();
        });
    }


 	getContrastAgent(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let protocolId = queryParams['id'];
                let mode = queryParams['mode'];
                if (mode) {
                    this.mode.setModeFromParameter(mode);
                }
                if (protocolId) {
                    // view or edit mode
                    this.protocol_id = protocolId;
                    return this.contrastAgentsService.getContrastAgent(this.protocol_id);
                } else {
                    // create mode
                    return Observable.of<ContrastAgent>();
                }
            })
            .subscribe(agent => {
                if (agent) {
                       agent.name = this.getReferenceById(agent.name);
                        agent.dose_unit = this.getReferenceById(agent.dose_unit);
                        agent.concentration_unit = this.getReferenceById(agent.concentration_unit);
                        this.agent = agent;
                }
                if (!this.mode.isCreateMode()) {
                     this.mode.createMode();
                }
            });

    }


    goToEditPage(): void {
        this.router.navigate(['/preclinical/contrastagent'], { queryParams: { id: this.agent.id, mode: "edit" } });
    }
    
    goToAddContrastAgent(){
        this.router.navigate(['/preclinical/reference'], { queryParams: { mode: "create", category:"contrastagent", reftype:"name" } });
    }

    ngOnInit(): void {
        this.getEnums();
        this.loadReferences();
        this.getContrastAgent();
        if (this.isStandalone) this.buildForm();
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
    }

    ngOnChange(): void {
        if (this.protocol_id && !this.mode.isCreateMode()) {
            this.getContrastAgent();
        }
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

    buildForm(): void {
        this.newContrastAgentForm = this.fb.group({
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

        this.newContrastAgentForm.valueChanges
            .subscribe(data => this.onValueChanged(data));
        this.onValueChanged();
    }

    onValueChanged(data?: any) {
        if (!this.newContrastAgentForm) { return; }
        const form = this.newContrastAgentForm;
        for (const field in this.formErrors) {
            // clear previous error message (if any)
            this.formErrors[field] = '';
            const control = form.get(field);
            if (control && control.dirty && !control.valid) {
                for (const key in control.errors) {
                    this.formErrors[field] += key;
                }
            }
        }
    }

    formErrors = {
        'name': ''
    };

    getOut(agent: ContrastAgent = null): void {
        if (this.closing.observers.length > 0) {
            this.closing.emit(agent);
            this.location.back();
        } else {
            this.location.back();
        }
    }

    addContrastAgent() {
        if (!this.agent) { return; }
        this.contrastAgentsService.create(this.protocol_id, this.agent)
            .subscribe(agent => {
                if (this.isStandalone) {
                    this.getOut(agent);
                } else {
                    this.closing.emit(agent);
                }
            });
    }

    updateContrastAgent(): void {
        this.contrastAgentsService.update(this.protocol_id, this.agent)
            .subscribe(agent => {
                this.getOut(agent);
            });
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