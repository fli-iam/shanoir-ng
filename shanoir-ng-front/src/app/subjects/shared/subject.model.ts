import { Examination } from '../../examinations/shared/examination.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { Id } from '../../shared/models/id.model';
import { ServiceLocator } from '../../utils/locator.service';
import { ImagedObjectCategory } from './imaged-object-category.enum';
import { SubjectStudy, SubjectStudyDTO } from './subject-study.model';
import { SubjectService } from './subject.service';
import { Sex } from './subject.types';

export class Subject extends Entity {

    id: number;
    examinations: Examination[];
    name: string;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    selected: boolean = false;
    subjectStudyList: SubjectStudy[] = [];

    public static makeSubject(id: number, name: string, identifier: string, subjectStudy: SubjectStudy): Subject {
        let subject = new Subject();
        subject.id = id;
        subject.name = name;
        subject.identifier = identifier;
        subject.subjectStudyList = [subjectStudy];
        return subject;
    }

    // Override
    public stringify() {
        console.log(this);
        return JSON.stringify(new SubjectDTO(this), this.replacer);
    }

    service = ServiceLocator.injector.get(SubjectService);
}


export class SubjectDTO {

    id: number;
    examinations: Id[];
    name: string;
    identifier: string;
    birthDate: Date;
    languageHemisphericDominance: "Left" | "Right";
    manualHemisphericDominance: "Left" | "Right";
    imagedObjectCategory: ImagedObjectCategory;
    sex: Sex;
    selected: boolean = false;
    subjectStudyList: Id[] = [];

    constructor(subject: Subject) {
        this.id = subject.id;
        if (subject.examinations) this.examinations = Id.toIdList(subject.examinations);
        this.name = subject.name;
        this.identifier = subject.identifier;
        this.birthDate = subject.birthDate;
        this.languageHemisphericDominance = subject.languageHemisphericDominance;
        this.manualHemisphericDominance = subject.manualHemisphericDominance;
        this.imagedObjectCategory = subject.imagedObjectCategory;
        this.sex = subject.sex;
        this.selected = subject.selected;
        this.subjectStudyList = subject.subjectStudyList ? subject.subjectStudyList.map(ss => {
            let dto = new SubjectStudyDTO(ss);
            dto.subject = null;
            return dto;
        }) : null;
    }

}
