import { Pipe, PipeTransform } from "@angular/core";

import { SubjectStudy } from "./subject-study.model";
import { SubjectType } from "./subject-type";

@Pipe({ name: "subjectStudyLabel" })
export class SubjectStudyPipe implements PipeTransform {

    transform(subjectStudy: SubjectStudy) {
        if (subjectStudy) {
            return subjectStudy.subjectStudyIdentifier + (subjectStudy.subjectType ? ' (' + SubjectType[subjectStudy.subjectType] + ')' : '');
        }
        return "";
    }

}