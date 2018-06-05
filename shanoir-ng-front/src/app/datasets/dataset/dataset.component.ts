import { Component, OnInit, Input } from '@angular/core';
import { Location } from '@angular/common';
import { Observable } from 'rxjs/Observable';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { DatasetService } from '../shared/dataset.service';
import { Dataset } from '../shared/dataset.model';
import { KeycloakService } from '../../shared/keycloak/keycloak.service';
import { MsgBoxService } from '../../shared/msg-box/msg-box.service';

@Component({
    selector: 'dataset-detail',
    templateUrl: 'dataset.component.html',
    styleUrls: ['dataset.component.css']
})

export class DatasetComponent implements OnInit {

    @Input() private mode: 'create' | 'edit' | 'view';
    private dataset: Dataset;
    public canModify: Boolean = false;
    
    constructor(
            private datasetService: DatasetService,
            private route: ActivatedRoute,
            private location: Location,
            private keycloakService: KeycloakService,
            private router: Router,
            private msgService: MsgBoxService) {}

    ngOnInit(): void {
        if (this.keycloakService.isUserAdmin() || this.keycloakService.isUserExpert()) {
            this.canModify = true;
        }
        this.fetchMode().then(() => {
            this.fetchDataset();
        });
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
                this.dataset = dataset;
            });
    }
    
    private back(): void {
        this.location.back();
    }

    private edit(): void {
        this.router.navigate([], {
            relativeTo: this.route, 
            queryParams: { id: this.dataset.id, mode: "edit" }
        });
    }

    private update(): void {
        this.datasetService.update(this.dataset).subscribe((dataset) => {
            this.msgService.log('info', 'Dataset updated');
        });
    }

    private download() {
        this.datasetService.download(this.dataset);
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