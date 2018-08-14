import { Pipe, PipeTransform } from "@angular/core";

import { SubjectStudy } from "./subject-study.model";

@Pipe({ name: "subjectStudyLabel" })
export class SubjectStudyPipe implements PipeTransform {

    transform(subjectStudy: SubjectStudy) {
        let displayedIdentifier: string = "";
        if (subjectStudy) {
            if (subjectStudy.subjectStudyIdentifier) {
                displayedIdentifier = subjectStudy.subjectStudyIdentifier;
            } else {
                displayedIdentifier = subjectStudy.subject.name;
            }
            displayedIdentifier += (subjectStudy.subjectType ? ' (' + subjectStudy.subjectType + ')' : '')
        }
        return displayedIdentifier;
    }
}