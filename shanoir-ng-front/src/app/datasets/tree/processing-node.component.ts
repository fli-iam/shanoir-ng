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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';

import { ProcessingNode, UNLOADED } from '../../tree/tree.model';
import { DatasetProcessing } from '../shared/dataset-processing.model';


@Component({
    selector: 'processing-node',
    templateUrl: 'processing-node.component.html'
})

export class ProcessingNodeComponent implements OnChanges {

    @Input() input: ProcessingNode | DatasetProcessing;
    @Output() selectedChange: EventEmitter<void> = new EventEmitter();
    node: ProcessingNode;
    loading: boolean = false;
    @Input() hasBox: boolean = false;

    constructor(
        private router: Router) {
    }
    
    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof ProcessingNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
        }
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.datasets) return false;
        else if (this.node.datasets == 'UNLOADED') return 'unknown';
        else return this.node.datasets.length > 0;
    }
}