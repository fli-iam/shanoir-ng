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

import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Router } from '@angular/router';
import { AcquisitionEquipment } from '../../acquisition-equipments/shared/acquisition-equipment.model';
import { CenterService } from '../../centers/shared/center.service';
import { ExaminationService } from '../../examinations/shared/examination.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { SubjectStudy } from '../../subjects/shared/subject-study.model';
import { BidsElement } from '../model/bidsElement.model'

@Component({
    selector: 'bids-tree',
    templateUrl: 'bids-tree.component.html',
    styleUrls: ['bids-tree.component.css'],
})

export class BidsTreeComponent  {

    @Input() list: BidsElement[];
    protected json: JSON;
    protected tsv: string;
    protected title: string;

    getFileName(element): string {
        return element.split('\\').pop().split('/').pop();
    }

    getDetail(component: TreeNodeComponent) {
        component.dataLoading = true;
        component.hasChildren = true;
        component.open();
    }

    getContent(bidsElem: BidsElement) {
        this.removeContent();
        if (bidsElem.content) {
            this.title = this.getFileName(bidsElem.path);
            if (bidsElem.path.indexOf('.json') != -1) {
                this.json = JSON.parse(bidsElem.content);
            } else if (bidsElem.path.indexOf('.tsv') != -1) {
                this.tsv = bidsElem.content;
            }
        }
    }

    removeContent() {
        this.title = null;
        this.tsv = null;
        this.json = null;
    }

}