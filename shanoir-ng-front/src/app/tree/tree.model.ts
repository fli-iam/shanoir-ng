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

import { ElementRef } from "@angular/core";
import { ExaminationDatasetAcquisitionDTO } from "../dataset-acquisitions/shared/dataset-acquisition.dto";
import { DatasetAcquisition } from "../dataset-acquisitions/shared/dataset-acquisition.model";
import { DatasetProcessing } from "../datasets/shared/dataset-processing.model";
import { Dataset } from "../datasets/shared/dataset.model";
import { DatasetProcessingType } from "../enum/dataset-processing-type.enum";
import { ExaminationPipe } from "../examinations/shared/examination.pipe";
import { SubjectExamination } from "../examinations/shared/subject-examination.model";
import { StudyUserRight } from "../studies/shared/study-user-right.enum";
import { SimpleStudy } from "../studies/shared/study.model";
import { QualityTag } from "../study-cards/shared/quality-card.model";
import { SubjectStudy } from "../subjects/shared/subject-study.model";
import { SubjectStudyPipe } from "../subjects/shared/subject-study.pipe";
import { Tag } from '../tags/tag.model';
import { SuperPromise } from "../utils/super-promise";

export abstract class ShanoirNode {
    
    abstract title: string;
    private _opened: boolean = false;
    private openPromise: Promise<void>;
    protected readonly routeBase: string;
    getTop: () => number; // to scroll to the node
    hidden: boolean = false;

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string
    ) {}

    public selected: boolean = false;

    open(): Promise<void> {
        this.hidden = false;
        if (!this._opened) {
            if (this.parent) {
                this.parent.open();
            }
            setTimeout(() => {
                // removing timeout may cause random bugs in the tree
                this._opened = true;
            });
            return SuperPromise.timeoutPromise().then(() => (this.openPromise || Promise.resolve()));
        } else {
            return Promise.resolve();
        }
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

    get route(): string {
        return this.routeBase + this.id;
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
        private qualityCards: QualityCardNode[] | UNLOADED,
        private members: MemberNode[] | UNLOADED,
        public rights: StudyUserRight[]
    ) {
        super(parent, id, label);
    }

    public subjectsNode: SubjectsNode = new SubjectsNode(this, null, 'Subjects', this.subjects);
    public centersNode: CentersNode = new CentersNode(this, null, 'Centers', this.centers);
    public studyCardsNode: StudyCardsNode = new StudyCardsNode(this, null, 'Study Cards', this.studyCards);
    public qualityCardsNode: QualityCardsNode = new QualityCardsNode(this, null, 'Quality Cards', this.qualityCards);
    public membersNode: MembersNode = new MembersNode(this, null, 'Members', this.members);
    title = 'study';
    protected readonly routeBase = '/study/details/';
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

    public title = "subjects";
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

    public title = "centers";
}

export abstract class CardsNode extends ShanoirNode {

    load: SuperPromise<void> = new SuperPromise();

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        private _cards: CardNode[] | UNLOADED
    ) {
        super(parent, id, label);
        this.registerOpenPromise(this.load);
    }

    set cards(cards: CardNode[] | UNLOADED) {
        this._cards = cards;
        if (cards && cards != UNLOADED) {
            this.load.resolve();
        }
    }

    get cards(): CardNode[] | UNLOADED {
        return this._cards;
    }

    public abstract title;
}

export class StudyCardsNode extends CardsNode {

    public title = "studycards";
}

export class QualityCardsNode extends CardsNode {

    public title = "qualitycards";
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

    public title = "members";
}


export abstract class SubjectNode extends ShanoirNode {
    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public tags: Tag[],
        public examinations: ExaminationNode[] | UNLOADED,
        public qualityTag: QualityTag,
        public canDeleteChildren: boolean,
        public canDownload: boolean
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

    public static fromSubjectStudy(subjectStudy: SubjectStudy, parent: ShanoirNode, canDeleteChildren: boolean, canDownload: boolean): ClinicalSubjectNode {
        return new ClinicalSubjectNode(
            parent,
            subjectStudy.subject.id,
            new SubjectStudyPipe().transform(subjectStudy),
            subjectStudy.tags,
            UNLOADED,
            subjectStudy.qualityTag,
            canDeleteChildren,
            canDownload);
    }

    protected readonly routeBase = '/subject/details/';
}

export class PreclinicalSubjectNode extends SubjectNode {
    public title = "preclinical-subject";
    public awesome = "fas fa-hippo";

    public static fromSubjectStudy(subjectStudy: SubjectStudy, parent: ShanoirNode, canDeleteChildren: boolean, canDownload: boolean): PreclinicalSubjectNode {
        return new PreclinicalSubjectNode(
            parent,
            subjectStudy.subject.id,
            new SubjectStudyPipe().transform(subjectStudy),
            subjectStudy.tags,
            UNLOADED,
            subjectStudy.qualityTag,
            canDeleteChildren,
            canDownload);
    }

    protected readonly routeBase = '/preclinical-subject/details/';
}


export class ExaminationNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public datasetAcquisitions: DatasetAcquisitionNode[] | UNLOADED,
        public extraDataFilePathList: string[] | UNLOADED,
        public canDelete: boolean,
        public canDownload,
        public preclinical: boolean
    ) {
        super(parent, id, label);
    }

    public selected: boolean = false;
    public extraDataOpen: boolean = false;
    public title: string = this.preclinical ? 'preclinical examination' : 'examination';
    protected readonly routeBase = this.preclinical ? '/preclinical-examination/details/' : '/examination/details/';

    public static fromExam(exam: SubjectExamination, parent: ShanoirNode, canDelete: boolean, canDownload: boolean): ExaminationNode {
        let node: ExaminationNode = new ExaminationNode(
            parent,
            exam.id,
            new ExaminationPipe().transform(exam),
            null,
            exam.extraDataFilePathList,
            canDelete,
            canDownload,
            exam.preclinical
        );
        node.datasetAcquisitions = exam.datasetAcquisitions ? exam.datasetAcquisitions.map(dsAcq => DatasetAcquisitionNode.fromAcquisition(dsAcq, node, canDelete, canDownload)) : [];
        return node;
    }
}


export class DatasetAcquisitionNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public datasets: DatasetNode[] | UNLOADED,
        public canDelete: boolean,
        public canDownload: boolean
    ) {
        super(parent, id, label);
    }

    public selected: boolean = false;
    public title: string = "dataset-acquisition";
    protected readonly routeBase = '/dataset-acquisition/details/';

    public static fromAcquisition(dsAcq: DatasetAcquisition | ExaminationDatasetAcquisitionDTO, parent: ShanoirNode, canDelete: boolean, canDownload: boolean): DatasetAcquisitionNode {
        let node: DatasetAcquisitionNode = new DatasetAcquisitionNode(
            parent,
            dsAcq.id,
            dsAcq.name,
            null,
            canDelete,
            canDownload
        );
        node.datasets = dsAcq.datasets ? dsAcq.datasets.map(ds => DatasetNode.fromDataset(ds, false, node, canDelete, canDownload)) : [];
        return node;
    }
}


export class DatasetNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public tags: Tag[],
        public type: string,
        public processings: ProcessingNode[] | UNLOADED,
        public processed: boolean,
        public canDelete: boolean,
        public canDownload: boolean,
        public inPacs: boolean,
        public metadata: MetadataNode
    ) {
        super(parent, id, label);
        this.tags = !tags ? [] : tags;
        if(processed){
            this.title = "processed-dataset";
            this.awesome = "fas fa-camera-rotate";
        }
    }

    public selected: boolean = false;
    public awesome: string = "fas fa-camera"
    public title: string = "dataset";
    protected readonly routeBase = '/dataset/details/';

    public static fromDataset(dataset: Dataset, processed: boolean, parent: ShanoirNode, canDelete: boolean, canDownload: boolean): DatasetNode {
        let node: DatasetNode = new DatasetNode(
            parent,
            dataset.id,
            dataset.name,
            dataset.tags,
            dataset.type,
            null,
            processed,
            canDelete,
            canDownload,
            dataset.inPacs,
            null
        );
        node.processings = dataset.processings ? dataset.processings.map(proc => ProcessingNode.fromProcessing(proc, node, canDelete, canDownload)) : [];
        let metadataNode: MetadataNode = new MetadataNode(node, node?.id, 'Dicom Metadata');
        node.metadata = metadataNode;
        return node;
    }
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
    protected readonly routeBase = '/dataset-processing/details/';

    public static fromProcessing(processing: DatasetProcessing, parent: ShanoirNode, canDelete: boolean, canDownload: boolean): ProcessingNode {
        let node: ProcessingNode = new ProcessingNode(
            parent,
            processing.id,
            processing.comment ? processing.comment : DatasetProcessingType.getLabel(processing.datasetProcessingType),
            null,
            canDelete
        );
        node.datasets = processing.outputDatasets ? processing.outputDatasets.map(ds => DatasetNode.fromDataset(ds, true, node, canDelete, canDownload)) : [];
        return node;
    }
}


export class CenterNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public acquisitionEquipments: AcquisitionEquipmentNode[] | UNLOADED,
        public coils: CoilNode[] | UNLOADED
    ) {
        super(parent, id, label);
    }

    public title: string = "center";
    protected readonly routeBase = '/center/details/';
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
    protected readonly routeBase = '/acquisition-equipment/details/';
}


export class CoilNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string
    ) {
        super(parent, id, label);
    }

    public title: string = "coil";
    protected readonly routeBase = '/coil/details/';
}


export abstract class CardNode extends ShanoirNode {

    constructor(
        public parent: ShanoirNode,
        public id: number,
        public label: string,
        public canDelete: boolean
    ) {
        super(parent, id, label);
    }

    public abstract type: 'studycard' | 'qualitycard';
    public abstract title: string;
}


export class StudyCardNode extends CardNode {

    public title: string = "study-card";
    public type: 'studycard' | 'qualitycard' = 'studycard';
    protected readonly routeBase = '/study-card/details/';
}


export class QualityCardNode extends CardNode {

    public title: string = "quality-card";
    public type: 'studycard' | 'qualitycard' = 'qualitycard';
    protected readonly routeBase = '/quality-card/details/';
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
    protected readonly routeBase = '/user/details/';
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
    protected readonly routeBase = '/subject/details/';
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
    protected readonly routeBase = '/study/details/';

    public static fromStudy(study: SimpleStudy, tags: Tag[], parent: ShanoirNode): ReverseStudyNode {
        return new ReverseStudyNode(
            parent,
            study.id,
            study.name,
            tags,
            UNLOADED
        );
    }
}

export class MetadataNode extends ShanoirNode {

    public title: string = "metadata";
    protected readonly routeBase = '/dataset/details/dicom/';
}
