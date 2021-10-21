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
import { Tag } from '../tags/tag.model';

interface ShanoirNode {
    open: boolean;
    id: number;
    label: string;
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
}


export class SubjectNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public tags: Tag[],
        public examinations: ExaminationNode[] | UNLOADED
    ) {}
        
    public open: boolean = false;
}


export class ExaminationNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public datasetAcquisitions: DatasetAcquisitionNode[] | UNLOADED,
        public extraDataFilePathList: string[] | UNLOADED
    ) {}

    public open: boolean = false;
    public extraDataOpen: boolean = false;
}


export class DatasetAcquisitionNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public datasets: DatasetNode[] | UNLOADED
    ) {}

    public open: boolean = false;
}


export class DatasetNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public type: string,
        public processings: ProcessingNode[] | UNLOADED
    ) {}

    public open: boolean = false;
    public selected: boolean = false;
}


export class ProcessingNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public datasets: DatasetNode[] | UNLOADED
    ) {}

    public open: boolean = false;
}


export class CenterNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public acquisitionEquipments: AcquisitionEquipmentNode[] | UNLOADED
    ) {}

    public open: boolean = false;
}


export class AcquisitionEquipmentNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public studyCards: StudyCardNode[] | UNLOADED
    ) {}

    public open: boolean = false;
}


export class StudyCardNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
    ) {}

    public open: boolean = false;
}


export class MemberNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public rights: RightNode[] | UNLOADED
    ) {}

    public open: boolean = false;
}


export class RightNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string
    ) {}

    public open: boolean = false;
}


export class ReverseSubjectNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public studies: ReverseStudyNode[] | UNLOADED
    ) {}
        
    public open: boolean = false;
}


export class ReverseStudyNode implements ShanoirNode {

    constructor(
        public id: number,
        public label: string,
        public tags: Tag[],
        public examinations: ExaminationNode[] | UNLOADED
    ) {}
        
    public open: boolean = false;
}