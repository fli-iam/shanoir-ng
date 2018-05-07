import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Params } from '@angular/router';
import { DatasetService } from '../shared/dataset.service';
import { Dataset } from '../shared/dataset.model';
import { DatasetType } from '../shared/dataset-type.enum';
import { SubjectService } from '../../subjects/shared/subject.service';
import { Subject } from '../../subjects/shared/subject.model';

@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css']
})

export class DatasetComponent implements OnInit {

    @Input() private mode: 'create' | 'edit' | 'view';
    private dataset: Dataset;
    private subjects: Subject[] = [];
    
    constructor(
            private datasetService: DatasetService,
            private subjectService: SubjectService,
            private route: ActivatedRoute,
            private location: Location) {}

    ngOnInit(): void {
        this.fetchMode().then(() => {
            this.fetchDataset();
        });
        this.fetchSubjects();
    }

    private fetchMode(): Promise<void> {
        return new Promise((resolve) => { 
            this.route.queryParams
                .subscribe((queryParams: Params) => {
                    if (this.mode = queryParams['mode']) this.mode = queryParams['mode'];
                    else this.mode = 'create';
                    resolve();
                });
        });
    }

    private fetchDataset(): void {
        this.route.queryParams
            .switchMap((queryParams: Params) => {
                let id = queryParams['id'];
                if (id) {
                    if (this.mode == 'create') throw Error('Cannot be in create mode and have an id');
                    return this.datasetService.get(id);
                } else {
                    if (this.mode != 'create') throw Error('Cannot be in '+this.mode+' mode without an id');
                    return Observable.of<Dataset>();
                }
            })
            .subscribe((dataset: Dataset) => {
                console.log(dataset);
                this.dataset = dataset;
            });
    }
    
    private fetchSubjects() {
        this.subjectService.getSubjects().then(subjects => {
            this.subjects = subjects;
        })
        .catch((error) => {
            // TODO: display error
        });
    }

    private getDatasetTypes(): any {
        return DatasetType.keyValues();
    }
    
    private back(): void {
        this.location.back();
    }


    // getFormValidationErrors() {
    //     Object.keys(this.subjectForm.controls).forEach(key => {
    //         const controlErrors: ValidationErrors = this.subjectForm.get(key).errors;
    //         if (controlErrors != null) {
    //             Object.keys(controlErrors).forEach(keyError => {
    //                 console.log('Key control: ' + key + ', keyError: ' + keyError + ', err value: ', controlErrors[keyError]);
    //             });
    //         }
    //     });
    // }

}