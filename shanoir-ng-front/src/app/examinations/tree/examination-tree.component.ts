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

import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { Examination } from '../shared/examination.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { ExaminationService } from '../shared/examination.service'

@Component({
    selector: 'examination-tree',
    templateUrl: 'examination-tree.component.html',
    styleUrls: ['examination-tree.component.css'],
})

export class ExaminationTreeComponent {

    constructor(private examinationService: ExaminationService,) {
        
    }

    @Input() examination: Examination;
    public fileIconPath: string = ImagesUrlUtil.FILE_ICON_PATH;
    public folderIconPath: string = ImagesUrlUtil.FOLDER_12_ICON_PATH;

    downloadFile(file) {
        this.examinationService.downloadFile(file, this.examination.id);
    }

}