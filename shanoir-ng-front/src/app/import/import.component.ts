import { Component } from '@angular/core';
import { TreeNodeComponent } from '../shared/tree/tree.node.component';

@Component({
    selector: 'import-modality',
    templateUrl: 'import.component.html',
    styleUrls: ['import.component.css']
})

export class ImportComponent {

    /* Form inputs */
    private modality: "IMR" | "PET";

    /* Display variables */
    private step: number = 1;
    private detailedSerie: Object;
    private thumbnailOn: boolean = false;
    private page: number = 1;

    /* Test data */
    private datasets = [
        {id: 1, name: "[MR] localizer 1", selected: true},
        {id: 2, name: "[MR] localizer 2", selected: true},
        {id: 3, name: "[MR] localizer 3", selected: true},
        {id: 4, name: "[MR] localizer 4", selected: false},
        {id: 5, name: "[MR] localizer 5", selected: true},
        {id: 6, name: "[MR] localizer 6", selected: true},
    ];

    constructor() {}

    fileChangeEvent(event: any) {

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
       
}