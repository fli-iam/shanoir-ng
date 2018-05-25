import { Component, Input, OnInit } from '@angular/core';
import { Dataset } from '../../shared/dataset.model';
import { ActivatedRoute, Params } from '@angular/router';
import { Subject } from '../../../subjects/shared/subject.model';
import { SubjectService } from '../../../subjects/shared/subject.service';
import { StudyService } from '../../../studies/shared/study.service';
import { Study } from '../../../studies/shared/study.model';


@Component({
    selector: 'common-dataset-details',
    templateUrl: 'dataset.common.component.html'
})

export class CommonDatasetComponent implements OnInit {

    @Input() private mode: 'create' | 'edit' | 'view';
    @Input() private dataset: Dataset;
    private subjects: Subject[] = [];
    private studies: Study[] = [];
    
    constructor(
            private studyService: StudyService,
            private subjectService: SubjectService) {}

    ngOnInit(): void {
        this.fetchSubjects();
        this.fetchStudies();
    }

    private fetchSubjects() {
        this.subjectService.getSubjects().then(subjects => {
            this.subjects = subjects;
        });
    }

    private fetchStudies() {
        this.studyService.getStudies().then(studies => {
            this.studies = studies;
        });
    }

}