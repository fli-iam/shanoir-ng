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

import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Router } from '@angular/router';

import { ToolService } from '../tool.service';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';
import { IdName } from '../../shared/models/id-name.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';

export class File {
    name: string
    url: string
    isDirectory: boolean
    format: string
    files: File[] = []
    constructor(url: string, format: string, isDirectory=false, files: File[] = []) {
        this.url = url;
        this.format = format;
        this.name = this.url.substring(this.url.lastIndexOf('/') + 1);
        this.isDirectory = isDirectory;
        this.files = files;
    }
}

@Component({
    selector: 'file-tree',
    templateUrl: 'file-tree.component.html',
    styleUrls: ['file-tree.component.css'],
})

export class FileTreeComponent {

    @Input() files: File[];
    @Output() fileSelected = new EventEmitter<File>();

    public fileIconPath: string = ImagesUrlUtil.FILE_ICON_PATH;
    public folderIconPath: string = ImagesUrlUtil.FOLDER_12_ICON_PATH;
    private listIconPath: string = ImagesUrlUtil.LIST_ICON_PATH;
    private xRay2IconPath: string = ImagesUrlUtil.X_RAY_2_ICON_PATH;

    constructor(private toolService: ToolService, private router: Router) {
    }
   
    getFiles(component: TreeNodeComponent) {
        component.hasChildren = true;
        component.open();
    }

    selectFile(file: File) {
        this.fileSelected.emit(file);
    }

    // If a child file is selected: emit fileSelected with the given file (bubble up to the root)
    onFileSelected(file: File) {
        this.selectFile(file);
    }

    hasChildren(file: File) {
        return file.files.length > 0;
    }

    getAwsome(file: File) {
        return this.hasChildren(file) ? "far fa-folder" : "";
    }
}