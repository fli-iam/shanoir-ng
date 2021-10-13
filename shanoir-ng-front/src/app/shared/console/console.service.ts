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

import { formatDate } from '@angular/common';
import { Injectable } from '@angular/core';
import { MsgBoxService } from '../msg-box/msg-box.service';

type MsgType = 'error' | 'warn' | 'info';
export class Message { constructor(public type: MsgType, public txt: string, public details: string[]) {} }

@Injectable()
export class ConsoleService {

    public messages: Message[] = [];

    constructor(private msgBoxService: MsgBoxService) {}


    public log(type: MsgType, txt: string, details?: string[]) {
        let dateStr: string = formatDate(new Date(), 'HH:mm', 'en');
        this.messages.unshift({type: type, txt: dateStr + ' - ' + txt, details: details});
        this.msgBoxService.log(type, txt);
    } 
}

