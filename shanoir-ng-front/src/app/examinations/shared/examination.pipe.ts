import { Pipe, PipeTransform } from "@angular/core";

import { Examination } from "./examination.model";

@Pipe({ name: "examinationLabel" })
export class ExaminationPipe implements PipeTransform {

    transform(examination: Examination) {
        if (examination) {
            return   new Date(examination.examinationDate).toLocaleDateString()  + " , ENCÉPHALIQUE  ( id = " + examination.id + " ) ";
        }
        return "";
    }

}