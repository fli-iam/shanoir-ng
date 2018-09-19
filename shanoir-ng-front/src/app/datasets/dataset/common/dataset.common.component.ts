import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Dataset } from '../../shared/dataset.model';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectService } from '../../../subjects/shared/subject.service';
import { StudyService } from '../../../studies/shared/study.service';
import { Study } from '../../../studies/shared/study.model';
import { NgForm, ControlContainer } from '@angular/forms';


@Component({
    selector: 'common-dataset-details',
    templateUrl: 'dataset.common.component.html',
    viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})

export class CommonDatasetComponent implements OnChanges {

    @Input() private mode: 'create' | 'edit' | 'view';
    @Input() private dataset: Dataset;
    private subjects: Subject[] = [];
    private studies: Study[] = [];
    

    constructor(
            private studyService: StudyService,
            private subjectService: SubjectService) {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes['mode']) {
            if (this.mode != 'view')  {
                this.fetchAllSubjects();
                this.fetchAllStudies();
            } else if (this.dataset) {
                this.fetchOneSubject();
                this.fetchOneStudy();
            }
        }
        if (changes['dataset'] && this.mode == 'view') {
            if (changes['dataset'].firstChange || changes['dataset'].previousValue.subjectId != changes['dataset'].currentValue.subjectId) {
                this.fetchOneSubject();
            }
            if (changes['dataset'].firstChange || changes['dataset'].previousValue.studyId != changes['dataset'].currentValue.studyId) {
                this.fetchOneStudy();
            }

        }
    }

    private fetchOneSubject() {
        if (!this.dataset.subjectId) return;
        this.subjectService.getSubject(this.dataset.subjectId).then(subject => {
            this.subjects = [subject];
        });
    }

    private fetchOneStudy() {
        if (!this.dataset.studyId) return;
        this.studyService.getStudy(this.dataset.studyId).then(study => {
            this.studies = [study];
        });
    }

    private fetchAllSubjects() {
        this.subjectService.getSubjects().then(subjects => {
            this.subjects = subjects;
        });
    }

    private fetchAllStudies() {
        this.studyService.getStudies().then(studies => {
            this.studies = studies;
        });
    }

    private getSubjectName(id: number): string {
        if (!this.subjects || this.subjects.length == 0 || !id) return null;
        for (let subject of this.subjects) {
            if (subject.id == id) return subject.name;
        }
        throw new Error('Cannot find subject for id = ' + id);
    }

    private getStudyName(id: number): string {
        if (!this.studies || this.studies.length == 0 || !id) return null;
        for (let study of this.studies) {
            if (study.id == id) return study.name;
        }
        throw new Error('Cannot find study for id = ' + id);
    }

}