import { Center } from '../../centers/shared/center.model';
import { Study } from './study.model';
import { Id } from '../../shared/models/id.model';

export class StudyCenter {
    center: Center;
    id: number;
    study: Study;
}

export class StudyCenterDTO {
    
    id: number;
    center: Id;
    study: Id;
   
    constructor(studyCenter: StudyCenter) {
        this.id = studyCenter.id;
        this.center = studyCenter.center ? new Id(studyCenter.center.id) : null;
        this.study = studyCenter.study ? new Id(studyCenter.study.id) : null;
    }
}