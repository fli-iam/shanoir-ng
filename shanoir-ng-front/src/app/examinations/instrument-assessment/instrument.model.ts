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

import { Examination } from '../shared/examination.model'

export class InstrumentBasedAssessment {
    id: number;
    instrument: Instrument;
    examination: Examination;
    variableAssessmentList: VariableAssessment[];
}

export class Instrument {
    id:number;
    acronym: string;
    childInstruments: Instrument[];
    instrumentBasedAssessments: InstrumentBasedAssessment[];
    instrumentDefinitionArticle: ScientificArticle;
    domains: number[];
    instrumentType: number;
    instrumentVariables: InstrumentVariable[];
    monoDomain: boolean;
    name: string;
    parentInstrument: Instrument;
    passationMode: number;

}

export class InstrumentVariable {
    id: number;
    ageDependent: boolean;
    culturalSkillDependent: boolean;
    instrument: Instrument;
    domain: number;
    main: boolean;
    name: string;
    quality: number;
    standardized: boolean;
    sexDependent: boolean;
    variableAssessmentList: VariableAssessment[];
}

export class CodedVariable extends InstrumentVariable {
    maxScaleItem: ScaleItem;
    minScaleItem: ScaleItem;
    scaleItemList: ScaleItem[];
}

export class NumericalVariable extends InstrumentVariable {
    maxScoreValue: number;
    minScoreValue: number;
}

export class VariableAssessment {
    id: number;
    instrumentBasedAssessment: InstrumentBasedAssessment;
    instrumentVariable: InstrumentVariable;
    scoreList: Score[];
}

export class ScientificArticle {
    id: number;
    scientificArticleReference: string;
    scientificArticleType: number;
}

export class Score {
    id: number;
    variableAssessment: VariableAssessment;
}

export class CodedScore extends Score {
    scaleItem: ScaleItem;
}

export class NumericalScore extends Score {
     isScoreWithUnitOfMeasure: boolean;
     refNumericalScoreType: string;
     refUnitOfMeasure: string;
     scoreStandardisationArticle: ScientificArticle;
     value: number;
}

export class ScaleItem {
     id: number;
     codedVariable: CodedVariable;
     correspondingNumber: number;
     qualitativeScaleItem: string;
     quantitativeScaleItem: string;
     refScaleItemType: string;
}