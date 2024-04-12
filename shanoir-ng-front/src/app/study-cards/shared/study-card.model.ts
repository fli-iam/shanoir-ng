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
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { Coil } from '../../coils/shared/coil.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { Study } from '../../studies/shared/study.model';


export class StudyCard extends Entity {

    id: number;
    name: string;
    study: Study;
    acquisitionEquipment: AcquisitionEquipment;
    rules: StudyCardRule[] = [];
}


export class StudyCardRule {

    constructor(public scope: MetadataFieldScope) {}

    assignments: StudyCardAssignment[];
    conditions: StudyCardCondition[];
    orConditions: boolean = false;

    static copy(rule: StudyCardRule): StudyCardRule {
        let copy: StudyCardRule = new StudyCardRule(rule.scope);
        copy.assignments = rule.assignments.map(ass => {
            let assCopy: StudyCardAssignment = new StudyCardAssignment(ass.scope);
            assCopy.field = ass.field;
            assCopy.value = ass.value;
            return assCopy;
        });
        copy.conditions = rule.conditions.map(con => {
            let conCopy: StudyCardCondition = new StudyCardCondition(con.scope);
            conCopy.dicomTag = con.dicomTag;
            conCopy.shanoirField = con.shanoirField;
            conCopy.values = [...con.values];
            conCopy.operation = con.operation;
            return conCopy;
        });
        return copy;
    }
}

export class StudyCardAssignment {
    
    field: string;
    value: string | Coil;
    
    constructor(public scope: MetadataFieldScope) {}
    
    get label(): string {
        if (this.value instanceof Coil) {
            return (this.value as Coil).name;
        } else {
            return this.value;
        }
    }

    get type(): 'string' | 'Coil' {
        if (this.value instanceof Coil) {
            return 'Coil';
        } else {
            return 'string';
        }
    }
}

export class StudyCardCondition {
    shanoirField: string;
    dicomTag: DicomTag;
    operation: Operation;
    values: (string | Coil)[] = [];
    cardinality: number;

    constructor(public scope: ConditionScope) {}

    get type(): 'string' | 'Coil' {
        if (this.values?.[0] instanceof Coil) {
            return 'Coil';
        } else {
            return 'string';
        }
    }
}

export type TagType = 'String' | 'Long' | 'Float' | 'Double' | 'Integer' | 'Binary' | 'Date' | 'FloatArray' | 'IntArray';

export type VM = {min: number, max: {number: number, multiplier: boolean}};

export class DicomTag {

    constructor(
        public code: number,
        public label: string,
        public type: TagType,
        public vm: VM) {};

    equals(other: DicomTag): boolean {
        return this.code == other.code;
    }
}

export type Operation = 'STARTS_WITH' | 'EQUALS' | 'ENDS_WITH' | 'CONTAINS' | 'DOES_NOT_CONTAIN' | 'SMALLER_THAN' | 'BIGGER_THAN' | 'DOES_NOT_START_WITH' | 'NOT_EQUALS' | 'DOES_NOT_END_WITH' | 'PRESENT' | 'ABSENT';

export type ConditionScope = 'StudyCardDICOMConditionOnDatasets' | 'AcqMetadataCondOnAcq' | 'AcqMetadataCondOnDatasets' | 
    'DatasetMetadataCondOnDataset' | 'ExamMetadataCondOnAcq' | 'ExamMetadataCondOnDatasets';

export type MetadataFieldScope = 'Dataset' | 'DatasetAcquisition';