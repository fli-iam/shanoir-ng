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

import { Component } from '@angular/core';
import { slideLeft} from '../animations/animations';
import { MsgBoxService } from './msg-box.service';

@Component({
    selector: 'msg-box',
    templateUrl: './msg-box.component.html',
    styleUrls: ['./msg-box.component.css'],
    animations: [slideLeft],
    standalone: false
})
export class MsgBoxComponent {


    constructor(private msgboxService: MsgBoxService) {
        
    }

    isOpened() {
      return this.msgboxService.isOpened();
    }

    getMsg() {
      return this.msgboxService.getMsg();
    }
}
