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
import { Component, ElementRef, Input, OnChanges, SimpleChanges } from '@angular/core';
import { TreeNodeAbstractComponent } from 'src/app/shared/components/tree/tree-node.abstract.component';

import { TreeService } from 'src/app/studies/study/tree.service';
import { CoilNode } from 'src/app/tree/tree.model';


@Component({
    selector: 'coil-node',
    templateUrl: 'coil-node.component.html',
    standalone: false
})

export class CoilNodeComponent extends TreeNodeAbstractComponent<CoilNode> implements OnChanges {

    @Input() input: CoilNode;
    detailsPath: string = '/coil/details/';

    constructor(
            protected treeService: TreeService,
            elementRef: ElementRef) {
        super(elementRef);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes['input']) {
            if (this.input instanceof CoilNode) {
                this.node = this.input;
            } else {
                throw new Error('not implemented yet');
            }
        }
    }
}
