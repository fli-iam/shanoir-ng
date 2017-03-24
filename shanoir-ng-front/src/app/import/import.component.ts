import { Component } from '@angular/core';
import { TreeNodeComponent } from '../shared/tree/tree.node.component';
import { Observable } from 'rxjs/Rx';
import { slideDown } from '../shared/animations/animations';

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css'],
    animations: [ slideDown ]
})

export class ImportComponent {

    /* Form inputs */
    private modality: "IMR" | "PET";
    private subjectMode: "single" | "group" = "single";
    private subject;
    private archive;

    /* Display variables */
    private step: number = 1;
    private detailedSerie: Object;
    private thumbnailOn: boolean = false;
    private page: number = 1;
    private dsAcqOpened: boolean = false;
    private createUser: boolean = false;

    private pacsProgress: number = 0;
    private pacsStatus: string;
    private anonymProgress: number = 0;
    private anonymStatus: string;
    private niftiProgress: number = 0;
    private niftiStatus: string;
    private studyCardProgress: number = 0;
    private studyCardStatus: string;

    private uploadProgress: number = 0;

    private tab_modality_open: boolean = true;
    private tab_upload_open: boolean = true;
    private tab_series_open: boolean = true;


    /* Test data */
    private datasets = [
        {id: 1, name: "[MR] localizer 1", selected: true},
        {id: 2, name: "[MR] localizer 2", selected: true},
        {id: 3, name: "[MR] localizer 3", selected: true},
        {id: 4, name: "[MR] localizer 4", selected: false},
        {id: 5, name: "[MR] localizer 5", selected: true},
        {id: 6, name: "[MR] localizer 6", selected: true},
    ];

    private subjects = [
        {id: 1, name: "ABVH3548"},
        {id: 2, name: "ABVH3548"},
        {id: 3, name: "ABVH6874"},
        {id: 4, name: "ABVH3548"},
        {id: 5, name: "ABVH9874"},
        {id: 6, name: "ABVH3548"}
    ];

    constructor() {}

    closeEditSubject(subject: any) {
        // Add the subject to the select box and select it
        console.log(subject);
        if (subject) {
            subject.name = subject.lastName;
            subject.selected = true;
            this.subjects.push(subject);
        }
        this.createUser = false;
    }

    showThumbnail(nodeParams: any) {
        if (nodeParams) {
            if (this.thumbnailOn && this.detailedSerie && nodeParams.id == this.detailedSerie["id"]) {
                this.thumbnailOn = false;
            } else {
                this.thumbnailOn = true;
                this.page = 1;
                this.detailedSerie = nodeParams;
            }
        }
    }

    showDetails(nodeParams: any) {
        this.thumbnailOn = false;
        if (nodeParams && this.detailedSerie && nodeParams.id == this.detailedSerie["id"]) {
            this.detailedSerie = null;
        } else {
            this.detailedSerie = nodeParams;
        }
    }

    over(button: any) {
        console.log(button);
        this.isOver = true;
    }

    private isOver = false;


    

    uploadArchive(event: any) {
        // TEST
        this.uploadProgress = 0;
        let subscription1 = Observable.timer(0,10).subscribe (t=> {
            this.uploadProgress = t*0.005;
            if (this.uploadProgress >= 1) {
                this.uploadProgress = 1;
                subscription1.unsubscribe();
                this.archive = "file uploaded";
                this.tab_upload_open = false;
            }
        });
    }
    

    startProgressTest() {
        this.pacsStatus = "";
        this.anonymStatus = "";
        this.niftiStatus = "";
        this.studyCardStatus = "";
        this.pacsProgress = 0;
        this.anonymProgress = 0;
        this.niftiProgress = 0;
        this.studyCardProgress = 0;
        let subscription1 = Observable.timer(0,10).subscribe (t=> {
            this.pacsProgress = t*0.005;
            if (this.pacsProgress >= 1) {
                this.pacsProgress = 1;
                this.pacsStatus = "ok";
                subscription1.unsubscribe();
                let subscription2 = Observable.timer(0,10).subscribe (t=> {
                    this.anonymProgress = t*0.002;
                    if (this.anonymProgress >= 1) {
                        this.anonymProgress = 1;
                        this.anonymStatus = "ok";
                        subscription2.unsubscribe();
                        let subscription3 = Observable.timer(0,10).subscribe (t=> {
                            this.niftiProgress = t*0.01;
                            if (this.niftiProgress >= 1) {
                                this.niftiProgress = 1;
                                this.niftiStatus = "ok";
                                subscription3.unsubscribe();
                                let subscription4 = Observable.timer(0,10).subscribe (t=> {
                                    this.studyCardProgress = t*0.01;
                                    if (this.studyCardProgress >= 0.3) {
                                        this.studyCardStatus = "error";
                                        subscription4.unsubscribe();
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }


}