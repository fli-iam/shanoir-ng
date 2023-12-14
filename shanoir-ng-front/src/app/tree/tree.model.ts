import { ExaminationPipe } from "../examinations/shared/examination.pipe";
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

import { SubjectExamination } from "../examinations/shared/subject-examination.model";
import { QualityTag } from "../study-cards/shared/quality-card.model";
import { Tag } from '../tags/tag.model';

interface ShanoirNode {
    open: boolean;
    id: number;
    label: string;
    title: string;
}

export type UNLOADED = 'UNLOADED';
export const UNLOADED: UNLOADED = 'UNLOADED';


export class StudyNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public subjects: SubjectNode[] | UNLOADED,
        public centers: CenterNode[] | UNLOADED,
        public studyCards: StudyCardNode[] | UNLOADED,
        public members: MemberNode[] | UNLOADED
    ) {}

    public open: boolean = false;
    public subjectsOpen: boolean = false;
    public centersOpen: boolean = false;
    public studycardsOpen: boolean = false;
    public membersOpen: boolean = false;
    public title = "study"
}


export abstract class SubjectNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public tags: Tag[],
        public examinations: ExaminationNode[] | UNLOADED,
        public qualityTag: QualityTag,
        public canDeleteChildren: boolean
    ) {
        if (!tags) tags = [];
        else tags = tags.map(t => t.clone());
        if (qualityTag) {
            let tag: Tag = new Tag();
            tag.id = -1;
            if (qualityTag == QualityTag.VALID) {
                tag.name = 'Valid';
                tag.color = 'green';
            } else if (qualityTag == QualityTag.WARNING) {
                tag.name = 'Warning';
                tag.color = 'chocolate';
            } else if (qualityTag == QualityTag.ERROR) {
                tag.name = 'Error';
                tag.color = 'red';
            }
            tags.unshift(tag);
        }
    }

    public open: boolean = false;
    public title: string;
    public awesome: string;
}

export class ClinicalSubjectNode extends SubjectNode {
    public title = "subject";
    public awesome = "fas fa-user-injured";
    qualityTag: QualityTag;
}

export class PreclinicalSubjectNode extends SubjectNode {
    public title = "preclinical-subject";
    public awesome = "fas fa-hippo";
}


export class ExaminationNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public datasetAcquisitions: DatasetAcquisitionNode[] | UNLOADED,
        public extraDataFilePathList: string[] | UNLOADED,
        public canDelete: boolean
    ) {}

    public open: boolean = false;
    public extraDataOpen: boolean = false;
    public title: string = "examination";
}


export class DatasetAcquisitionNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public datasets: DatasetNode[] | UNLOADED,
        public canDelete: boolean
    ) {}

    public open: boolean = false;
    public title: string = "dataset-acquisition";
}


export class DatasetNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public type: string,
        public processings: ProcessingNode[] | UNLOADED,
        public processed: boolean,
        public canDelete: boolean
    ) {}

    public open: boolean = false;
    public selected: boolean = false;

    public title: string = "dataset";

}


export class ProcessingNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public datasets: DatasetNode[] | UNLOADED,
        public canDelete: boolean
    ) {}

    public open: boolean = false;
    public title: string = "processing";
}


export class CenterNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public acquisitionEquipments: AcquisitionEquipmentNode[] | UNLOADED
    ) {}

    public open: boolean = false;
    public title: string = "center";
}


export class AcquisitionEquipmentNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public studyCards: StudyCardNode[] | UNLOADED,
        public canDelete: boolean
    ) {}

    public open: boolean = false;
    public title: string = "acquisition-equipment";
}


export class StudyCardNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public canDelete: boolean
    ) {}

    public open: boolean = false;
    public title: string = "study-card";
}


export class MemberNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public rights: RightNode[] | UNLOADED
    ) {}

    public open: boolean = false;
    public title: string = "member";
}


export class RightNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string
    ) {}

    public open: boolean = false;
    public title: string = "right";
}


export class ReverseSubjectNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public studies: ReverseStudyNode[] | UNLOADED
    ) {}

    public open: boolean = false;
    public title: string = "subject";
}


export class ReverseStudyNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public tags: Tag[],
        public examinations: ExaminationNode[] | UNLOADED
    ) {}

    public open: boolean = false;
    public title: string = "study";
}
