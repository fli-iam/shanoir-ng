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

import { QualityTag } from "../study-cards/shared/quality-card.model";
import { Tag } from '../tags/tag.model';
import { SuperPromise } from "../utils/super-promise";

export abstract class ShanoirNode {
    abstract title: string;
    private _opened: boolean = false;
    private openPromise: Promise<void>;

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string
    ) {}

    open(): Promise<void> {
        if (this.parent) {
            this.parent.open();
        }
        this._opened = true;
        return (this.openPromise || Promise.resolve()).then(() => SuperPromise.timeoutPromise());
    }

    close() {
        this._opened = false;
    }

    registerOpenPromise(promise: Promise<void>) {
        this.openPromise = promise;
    }

    get opened(): boolean {
        return this._opened;
    }

    set opened(opened: boolean) {
        opened ? this.open() : this.close();
    }
}

export type UNLOADED = 'UNLOADED';
export const UNLOADED: UNLOADED = 'UNLOADED';


export class StudyNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        private subjects: SubjectNode[] | UNLOADED,
        private centers: CenterNode[] | UNLOADED,
        private studyCards: StudyCardNode[] | UNLOADED,
        private members: MemberNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public subjectsNode: SubjectsNode = new SubjectsNode(this, null, 'Subjects', this.subjects);
    public centersNode: CentersNode = new CentersNode(this, null, 'Centers', this.centers);
    public studyCardsNode: StudyCardsNode = new StudyCardsNode(this, null, 'Study Cards', this.studyCards);
    public membersNode: MembersNode = new MembersNode(this, null, 'Members', this.members);
    public title = "study"
}

export class SubjectsNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public subjects: SubjectNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title = "subjects"
}

export class CentersNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public centers: CenterNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title = "centers"
}

export class StudyCardsNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public studycards: StudyCardNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title = "studycards"
}

export class MembersNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public members: MemberNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title = "members"
}


export abstract class SubjectNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public tags: Tag[],
        public examinations: ExaminationNode[] | UNLOADED,
        public qualityTag: QualityTag,
        public canDeleteChildren: boolean
    ) {
        super(parent, id, label);
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


export class ExaminationNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public datasetAcquisitions: DatasetAcquisitionNode[] | UNLOADED,
        public extraDataFilePathList: string[] | UNLOADED,
        public canDelete: boolean
    ) {
        super(parent, id, label);
    }

    public extraDataOpen: boolean = false;
    public title: string = "examination";
}


export class DatasetAcquisitionNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public datasets: DatasetNode[] | UNLOADED,
        public canDelete: boolean
    ) {
        super(parent, id, label);
    }

    public title: string = "dataset-acquisition";
}


export class DatasetNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public type: string,
        public processings: ProcessingNode[] | UNLOADED,
        public processed: boolean,
        public canDelete: boolean
    ) {
        super(parent, id, label);
    }

    public selected: boolean = false;

    public title: string = "dataset";

}


export class ProcessingNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public datasets: DatasetNode[] | UNLOADED,
        public canDelete: boolean
    ) {
        super(parent, id, label);
    }

    public title: string = "processing";
}


export class CenterNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public acquisitionEquipments: AcquisitionEquipmentNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title: string = "center";
}


export class AcquisitionEquipmentNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public studyCards: StudyCardNode[] | UNLOADED,
        public canDelete: boolean
    ) {
        super(parent, id, label);
    }

    public title: string = "acquisition-equipment";
}


export class StudyCardNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public canDelete: boolean
    ) {
        super(parent, id, label);
    }

    public title: string = "study-card";
}


export class MemberNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public rights: RightNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title: string = "member";
}


export class RightNode extends ShanoirNode {

    public title: string = "right";
}


export class ReverseSubjectNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public studies: ReverseStudyNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title: string = "subject";
}


export class ReverseStudyNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public tags: Tag[],
        public examinations: ExaminationNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title: string = "study";
}