import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';


import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { Examination } from '../shared/examination.model';

@Component({
    selector: 'examination-tree',
    templateUrl: 'examination-tree.component.html',
    styleUrls: ['examination-tree.component.css'],
})

export class ExaminationTreeComponent {

    @Input() examination: Examination;
    

    constructor(private router: Router) {
    }


}