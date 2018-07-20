import { Component, Input, Output, EventEmitter } from '@angular/core';
import { IdNameObject } from '../../models/id-name-object.model';
import { SubjectStudy } from '../../../subjects/shared/subject-study.model';
import { Study } from '../../../studies/shared/study.model';
import { Subject } from '../../../subjects/shared/subject.model';

@Component({
  selector: 'subject-study-list',
  templateUrl: 'subject-study-list.component.html',
  styleUrls: ['subject-study-list.component.css'],
})

export class SubjectStudyListComponent{
    @Input() mode: string = "study | subject";
    @Input() list: any[];
    @Output() ssl = new EventEmitter<SubjectStudy[]>();
    
    private legend: string = this.mode == 'study' ? 'Studies' : 'Subjects';
    private columnName: string = this.mode == 'study' ? 'Common Name' : 'Study Name';
    private subjectStudyList: SubjectStudy[] = [];

    private onChangeSubjectStudyList() {
        this.ssl.emit(this.subjectStudyList);
    }

    onObjectSelect(object: IdNameObject) {
        object.selected = true;
        let newSubjectStudy: SubjectStudy = new SubjectStudy();
        newSubjectStudy.physicallyInvolved = false;
        if (this.mode == "study") {
            newSubjectStudy.study = new Study(object);
        } else if (this.mode == "subject") {
            newSubjectStudy.subject = new Subject(object);
        }
        this.subjectStudyList.push(newSubjectStudy);
        this.onChangeSubjectStudyList();
    }

    removeSubjectStudy(subjectStudy: SubjectStudy):void {
        for (let object of this.list) {
            if (this.mode == "study") {
                if (subjectStudy.study.id == object.id) object.selected = false;
            } else if (this.mode == "subject") {
                if (subjectStudy.subject.id == object.id) object.selected = false;
            }
        }
        const index: number = this.subjectStudyList.indexOf(subjectStudy);
        if (index !== -1) {
            this.subjectStudyList.splice(index, 1);
        }
        this.onChangeSubjectStudyList();
    }
}
