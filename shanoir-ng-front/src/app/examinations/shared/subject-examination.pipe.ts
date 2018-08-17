import { Pipe, PipeTransform } from "@angular/core";

import { SubjectExamination } from "./subject-examination.model";

@Pipe({ name: "subjectExaminationLabel" })
export class SubjectExaminationPipe implements PipeTransform {

    transform(examination: SubjectExamination) {
        if (examination) {
            return new Date(examination.examinationDate).toLocaleDateString()
                + ", " + examination.comment + " ( id = " + examination.id + " ) ";
        }
        return "";
    }

}