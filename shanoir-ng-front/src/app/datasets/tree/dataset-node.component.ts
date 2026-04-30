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
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';

import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';
import { TreeService } from 'src/app/studies/study/tree.service';

import { MassDownloadService } from "../../shared/mass-download/mass-download.service";
import { DatasetNode, ProcessingNode, UNLOADED } from '../../tree/tree.model';
import { Dataset } from '../shared/dataset.model';
import { DatasetService } from '../shared/dataset.service';
import { DatasetProcessingService } from '../shared/dataset-processing.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { DropdownMenuComponent } from '../../shared/components/dropdown-menu/dropdown-menu.component';
import { MenuItemComponent } from '../../shared/components/dropdown-menu/menu-item/menu-item.component';

import { MetadataNodeComponent } from './metadata-node.component';
import { ProcessingNodeComponent } from './processing-node.component';


@Component({
    selector: 'dataset-node',
    templateUrl: 'dataset-node.component.html',
    imports: [TreeNodeComponent, FormsModule, DropdownMenuComponent, RouterLink, MenuItemComponent, MetadataNodeComponent, ProcessingNodeComponent]
})

export class DatasetNodeComponent extends TreeNodeAbstractComponent<DatasetNode> implements OnChanges {

    @Input() input: DatasetNode | Dataset;
    @Input() related: boolean = false;
    detailsPath: string = '/dataset/details/';
    @Output() datasetDelete: EventEmitter<void> = new EventEmitter();

    constructor(
            private router: Router,
            private datasetService: DatasetService,
            private downloadService: MassDownloadService,
            protected treeService: TreeService,
            private processingService: DatasetProcessingService,
            elementRef: ElementRef) {
        super(elementRef);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof DatasetNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
            this.node.registerOpenPromise(this.contentLoaded);
            this.nodeInit.emit(this.node);
        }
    }

    toggleMenu() {
        this.menuOpened = this.withMenu && !this.menuOpened;
    }

    download() {
        if (this.loading) {
            return;
        }
        this.loading = true;
        this.downloadService.downloadByIds([this.node.id], this.downloadState)
            .then(() => this.loading = false);
    }

    hasChildren(): boolean | 'unknown' {
        if (this.node.inPacs) {
            return true;
        } else {
            if (!this.node.processings) return false;
            else if (this.node.processings == 'UNLOADED') return 'unknown';
            else return this.node.processings.length > 0;
        }
    }

    deleteDataset() {
        this.datasetService.get(this.node.id).then(entity => {
            this.datasetService.deleteWithConfirmDialog(this.node.title, entity).then(deleted => {
                if (deleted) {
                    this.datasetDelete.emit();
                }
            });
        })
    }

    onProcessingDelete(index: number) {
        (this.node.processings as ProcessingNode[]).splice(index, 1) ;
    }

    loadProcessings() {
        if (this.node.processings == UNLOADED) {
            this.loading = true;
            this.processingService.findByInputDatasetId(this.node.id).then(processings => {
                this.node.processings = processings.map(p => ProcessingNode.fromProcessing(p, this.node, this.node.canDelete, this.node.canDownload));
            }).finally(() => {
                this.loading = false;
                this.contentLoaded.resolve();
            });
        } else {
            this.contentLoaded.resolve();
        }
    }
}
