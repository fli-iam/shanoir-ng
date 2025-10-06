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
import { QualityCard, QualityTag } from './quality-card.model';
import { StudyCardConditionDTO } from './study-card.dto.model';


export class QualityCardDTO {

    id: number;
    name: string;
    studyId: number;
    rules: QualityCardRuleDTO[];
    toCheckAtImport: boolean = false;

    constructor(qualityCard?: QualityCard) {
        if (qualityCard) {
            this.id = qualityCard.id;
            this.name = qualityCard.name;
            this.studyId = qualityCard.study ? qualityCard.study.id : this.studyId;
            this.rules = qualityCard.rules.map(rule => {
                const ruleDTO: QualityCardRuleDTO = new QualityCardRuleDTO();
                ruleDTO.conditions = rule.conditions.map(cond => new StudyCardConditionDTO(cond));
                ruleDTO.qualityTag = rule.tag;
                ruleDTO.orConditions = rule.orConditions;
                return ruleDTO;
            });
            this.toCheckAtImport = qualityCard.toCheckAtImport;
        }
    }
}

export class QualityCardRuleDTO {
    qualityTag: QualityTag[];
    conditions: StudyCardConditionDTO[];
    orConditions: boolean;
}
