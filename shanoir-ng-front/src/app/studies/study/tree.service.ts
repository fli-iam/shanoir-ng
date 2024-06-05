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

import { Injectable } from "@angular/core";
import { Subject as RxjsSubject } from "rxjs";
import { AcquisitionEquipment } from 'src/app/acquisition-equipments/shared/acquisition-equipment.model';
import { Center } from 'src/app/centers/shared/center.model';
import { DatasetAcquisition } from 'src/app/dataset-acquisitions/shared/dataset-acquisition.model';
import { DatasetProcessing } from 'src/app/datasets/shared/dataset-processing.model';
import { Dataset } from 'src/app/datasets/shared/dataset.model';
import { Examination } from 'src/app/examinations/shared/examination.model';
import { QualityCard } from 'src/app/study-cards/shared/quality-card.model';
import { StudyCard } from 'src/app/study-cards/shared/study-card.model';
import { Subject } from "src/app/subjects/shared/subject.model";
import { User } from 'src/app/users/shared/user.model';
import { Study } from "../shared/study.model";

@Injectable()
export class TreeService {

    _selection: Selection = null;
    public change: RxjsSubject<Selection> = new RxjsSubject();

    isSelected(id: number, type: NodeType): boolean {
        return this.selection?.isSelected(id, type);
    }
    
    get selection(): Selection {
        return this._selection;
    }

    set selection(selection: Selection) {
        this._selection = selection;
        this.change.next(this.selection);
    }

    // /** when you know the new selected node's study is the one displayed */
    // update(id: number, type: NodeType) {
    //     this.selection.id = id;
    //     this.selection.type = type;
    //     this.change.next(this.selection);
    // }
   
}

export type NodeType = 'study' | 'subject' | 'examination' | 'acquisition' | 'dataset' | 'processing' | 'center' | 'equipment' | 'studycard' | 'qualitycard' | 'user' | 'dicomMetadata';

export class Selection {

    constructor(
        public id: number,
        public type: NodeType,
        public studyId: number[]
    ) {}

    isSelected(id: number, type: NodeType): boolean {
        return id == this.id && type == this.type;
    }

    static fromStudy(study: Study): Selection {
        return new Selection(study.id, 'study', [study.id]);
    }

    static fromSubject(subject: Subject): Selection {
        return new Selection(subject.id, 'subject', subject.subjectStudyList.map(ss => ss.study.id));
    }

    static fromExamination(examination: Examination): Selection {
        return new Selection(examination.id, 'examination', [examination.study.id]);
    }

    static fromAcquisition(acquisition: DatasetAcquisition): Selection {
        return new Selection(acquisition.id, 'acquisition', [acquisition.examination.study.id]);
    }

    static fromDataset(dataset: Dataset): Selection {
        return new Selection(dataset.id, 'dataset', [dataset.datasetProcessing ? dataset.datasetProcessing.outputDatasets?.[0]?.study.id : dataset.datasetAcquisition.examination.study.id]);
    }

    static fromProcessing(processing: DatasetProcessing): Selection {
        return new Selection(processing.id, 'processing', [processing.studyId]);
    }

    static fromCenter(center: Center): Selection {
        return new Selection(center.id, 'center', center.studyCenterList.map(sc => sc.study.id));
    }

    static fromEquipment(equipment: AcquisitionEquipment): Selection {
        return new Selection(equipment.id, 'equipment', equipment.center.studyCenterList?.map(sc => sc.study.id));
    }

    static fromStudycard(studycard: StudyCard): Selection {
        return new Selection(studycard.id, 'studycard', [studycard.study.id]);
    }

    static fromQualitycard(qualitycard: QualityCard): Selection {
        return new Selection(qualitycard.id, 'qualitycard', [qualitycard.study.id]);
    }

    static fromUser(user: User): Selection {
        console.log(user.studyUserList)
        return new Selection(user.id, 'user', user.studyUserList?.map(su => su.studyId));
    }

    static fromDicomMetadata(dataset: Dataset): Selection {
        return new Selection(dataset.id, 'dicomMetadata', [dataset.datasetAcquisition.examination.study.id]);
    }
}