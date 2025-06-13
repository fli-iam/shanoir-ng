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
import { Component, ElementRef, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Router } from '@angular/router';

import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { TreeService } from 'src/app/studies/study/tree.service';
import { DatasetNode, ProcessingNode, UNLOADED } from '../../tree/tree.model';
import { DatasetProcessing } from '../shared/dataset-processing.model';
import { DatasetProcessingService } from "../shared/dataset-processing.service";


@Component({
    selector: 'processing-node',
    templateUrl: 'processing-node.component.html',
    standalone: false
})

export class ProcessingNodeComponent extends TreeNodeAbstractComponent<ProcessingNode> implements OnChanges {

    @Input() input: ProcessingNode | DatasetProcessing;
    @Output() onProcessingDelete: EventEmitter<void> = new EventEmitter();

    constructor(
            private router: Router,
            private processingService: DatasetProcessingService,
            protected treeService: TreeService,
            elementRef: ElementRef) {
        super(elementRef);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof ProcessingNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
            this.node.registerOpenPromise(this.contentLoaded);
            this.nodeInit.emit(this.node);
        }
    }

    toggleMenu() {
        this.menuOpened = !this.menuOpened;
    }

    showProcessingDetails() {
        this.router.navigate(['/dataset-processing/details/' + this.node.id])
    }

    hasChildren(): boolean | 'unknown' {
        if (!this.node.datasets) return false;
        else if (this.node.datasets == 'UNLOADED') return 'unknown';
        else return this.node.datasets.length > 0;
    }

    onSimpleDatasetDelete(index: number) {
        (this.node.datasets as DatasetNode[]).splice(index, 1) ;
    }

    deleteProcessing() {
        this.processingService.get(this.node.id).then(entity => {
            this.processingService.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.onProcessingDelete.emit();
                }
            });
        })
    }

    loadOutputDatasets() {
        if (this.node.datasets == UNLOADED) {
            this.loading = true;
            this.processingService.getOutputDatasets(this.node.id).then(datasets => {
                this.node.datasets = datasets.map(d => DatasetNode.fromDataset(d, true, this.node, this.node.canDelete, this.node.canDownload));
            }).finally(() => {
                this.loading = false;
                this.contentLoaded.resolve();
            });
        } else {
            this.contentLoaded.resolve();
        }
    }
}
