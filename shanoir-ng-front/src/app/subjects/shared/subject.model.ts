import { Examination } from '../../examinations/shared/examination.model';
import { Entity } from '../../shared/components/entity/entity.abstract';
import { ServiceLocator } from '../../utils/locator.service';
import { ImagedObjectCategory } from './imaged-object-category.enum';
import { SubjectStudy } from './subject-study.model';
import { SubjectService } from './subject.service';
import { Sex } from './subject.types';

export class Subject extends Entity {

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

    service = ServiceLocator.injector.get(SubjectService);
}
