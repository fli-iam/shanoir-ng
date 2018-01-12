import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

import { Examination } from '../shared/examination.model';
import { ImagesUrlUtil } from '../../shared/utils/images-url.util';
import { TreeNodeComponent } from '../../shared/components/tree/tree-node.component';

@Component({
    selector: 'examination-tree',
    templateUrl: 'examination-tree.component.html',
    styleUrls: ['examination-tree.component.css'],
})

export class ExaminationTreeComponent {

    @Input() examination: Examination;
    public fileIconPath: string = ImagesUrlUtil.FILE_ICON_PATH;
    public folderIconPath: string = ImagesUrlUtil.FOLDER_12_ICON_PATH;

}