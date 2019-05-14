import { Pipe, PipeTransform } from "@angular/core";

import { SubjectStudy } from "./subject-study.model";
import { IdName } from "../../shared/models/id-name.model";

@Pipe({ name: "studyNamePipe" })
export class StudyNamePipe implements PipeTransform {

    transform(subjectStudy: SubjectStudy, studies: IdName[]) {
        if (subjectStudy && studies) {
            for (let study of studies) {
                if(subjectStudy.study.id == study.id)
                    return study.name;
            }
        }
        return "";
    }

}