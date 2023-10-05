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

import {Component, ComponentRef, ElementRef, EventEmitter, HostListener, Input, Output, ViewChild} from '@angular/core';
import {Study} from "../../../studies/shared/study.model";
import {UntypedFormBuilder, UntypedFormGroup} from "@angular/forms";
import { GlobalService } from '../../services/global.service';

@Component({
    selector: 'user-action-dialog',
    templateUrl: 'dataset-copy-dialog.component.html',
    styleUrls: ['dataset-copy-dialog.component.css']
})
export class DatasetCopyDialogComponent {
    title: string;
    message: string;
    studies: Study[];
    selectedStudyIds: Set<Study> = new Set();
    datasetsIds: number[];
    checkboxMode: 'edit';
    statusMessage: string;
    ownRef: any;

    cancel() {
        this.ownRef.destroy();
    }

    public copy() {
        console.log("copy");
        for (let item of Array.from(this.selectedStudyIds.values())) {
            console.log(item.id + " / " + item.name);
        }
    }

    pickStudy(study: Study) {
        if (!this.selectedStudyIds.has(study)) {
            this.selectedStudyIds.add(study);
        } else {
            this.selectedStudyIds.delete(study);
        }
    }
}
