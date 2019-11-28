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

import { Component, forwardRef, Input } from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

import { Study } from '../../../studies/shared/study.model';
import { SubjectStudy } from '../../../subjects/shared/subject-study.model';
import { Subject } from '../../../subjects/shared/subject.model';
import { AbstractInput } from '../../form/input.abstract';

@Component({
  selector: 'subject-study-list',
  templateUrl: 'subject-study-list.component.html',
  styleUrls: ['subject-study-list.component.css'],
  providers: [
    { 
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => SubjectStudyListComponent),
      multi: true
    }
]
})

export class SubjectStudyListComponent extends AbstractInput {
    
    @Input() subject: Subject;
    @Input() study: Study;
    @Input() selectableList: Subject[] | Study[];
    @Input() displaySubjectType: boolean = true;
    private selected: any;

    private get legend(): string {
        return this.compMode == 'study' ? 'Subjects' : 'Studies';
    }

    writeValue(obj: any): void {
        super.writeValue(obj);
        if (this.model && this.selectableList) {
            if (this.compMode == 'study') {
                for (let selectableItem of this.selectableList) {
                    if(this.model.find(subStu => subStu.subject.id == selectableItem.id)) selectableItem.selected = true; 
                }
            } else if (this.compMode == 'subject') {
                for (let selectableItem of this.selectableList) {
                    if(this.model.find(subStu => subStu.study.id == selectableItem.id)) selectableItem.selected = true;
                }
            }
        }
    }

    private get compMode(): 'subject' | 'study' { 
        if (this.subject && this.study) throw Error('You cannot set both subject and study');
        if (this.subject) return 'subject';
        if (this.study) return 'study';
        throw Error('You have to set either subject or study');
        
    }

    onAdd() {
        if (!this.selected) return;
        this.selected.selected = true;
        let newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        if (this.compMode == "study") {
            let studyCopy: Study = new Study();
            studyCopy.id = this.study.id;
            newSubjectStudy.study = studyCopy;
            newSubjectStudy.subject = this.selected;
        }
        else if (this.compMode == "subject") {
            let subjectCopy: Subject = new Subject();
            subjectCopy.id = this.subject.id;
            newSubjectStudy.subject = subjectCopy;
            newSubjectStudy.study = this.selected;
        }
        this.selected = undefined;
        this.model.push(newSubjectStudy);
        this.propagateChange(this.model);
    }

    removeSubjectStudy(subjectStudy: SubjectStudy):void {
        const index: number = this.model.indexOf(subjectStudy);
        if (index > -1) {
            this.model.splice(index, 1);
            this.propagateChange(this.model);
            if (this.compMode == 'study') {
                for (let selectableItem of this.selectableList) {
                    if (selectableItem.id == subjectStudy.subject.id) {
                        selectableItem.selected = false;
                    }
                }
            } else if (this.compMode == 'subject') {
                for (let selectableItem of this.selectableList) {
                    if (selectableItem.id == subjectStudy.study.id) {
                        selectableItem.selected = false;
                    }
                }
            }
        }
    }

    onChange() {
        this.propagateChange(this.model);
    }

    onTouch() {
        this.propagateTouched();
    }
}
