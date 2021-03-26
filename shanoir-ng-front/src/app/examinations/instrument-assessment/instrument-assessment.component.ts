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

import { Component, EventEmitter, OnInit, Output, Input } from '@angular/core';
import { InstrumentBasedAssessment, Instrument, VariableAssessment } from '../instrument-assessment/instrument.model'

@Component({
    selector: 'instrument-assessment-detail',
    templateUrl: 'instrument-assessment.component.html'
})

export class InstrumentAssessmentComponent {
    @Input() instrumentBasedAssesment: InstrumentBasedAssessment;

    getInstrumentVarName(varAssess: VariableAssessment, instrument: Instrument): string {
        for (let varia of instrument.instrumentVariables) {
            for (let ass of varia.variableAssessmentList) {
                if (ass.id == varAssess.id) {
                    return varia.name;
                }
            }
        }
        return 'not found';
    }
}