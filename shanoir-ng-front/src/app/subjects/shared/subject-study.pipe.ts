import { Pipe, PipeTransform } from "@angular/core";

import { SubjectStudy } from "./subject-study.model";
import { SubjectType } from '../../subjects/shared/subject.types';

@Pipe({ name: "subjectStudyLabel" })
export class SubjectStudyPipe implements PipeTransform {

    transform(subjectStudy: SubjectStudy) {
        if (subjectStudy) {
            return subjectStudy.subjectStudyIdentifier + (subjectStudy.subjectType ? ' (' + subjectStudy.subjectType + ')' : '');
        }
        return "";
    }

}