import { Pipe, PipeTransform } from "@angular/core";

import { SubjectStudy } from "./subject-study.model";
import { SubjectType } from '../../subjects/shared/subject.types';
import { IdNameObject } from "../../shared/models/id-name-object.model";

@Pipe({ name: "studyNamePipe" })
export class StudyNamePipe implements PipeTransform {

    transform(subjectStudy: SubjectStudy, studies: IdNameObject[]) {
        if (subjectStudy && studies) {
            for (let study of studies) {
                if(subjectStudy.study.id == study.id)
                    return study.name;
            }
        }
        return "";
    }

}