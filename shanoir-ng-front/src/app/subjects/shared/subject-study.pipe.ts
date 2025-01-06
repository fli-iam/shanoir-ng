/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

import { Pipe, PipeTransform } from "@angular/core";

import { SubjectStudy } from "./subject-study.model";

@Pipe({
    name: "subjectStudyLabel",
    standalone: false
})
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