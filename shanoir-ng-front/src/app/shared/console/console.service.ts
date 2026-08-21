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
import { Observable, Observer } from 'rxjs';

import { MsgBoxService } from '../msg-box/msg-box.service';

type MsgType = 'error' | 'warn' | 'info';
export class Message { 
    fresh: boolean;
    detailsOpened: boolean = false;
    nb: number = 1;

    constructor(public type: MsgType, public txt: string, public details: string[]) {
        this.fresh = true;
        setTimeout(() => this.fresh = false, 30000);
    } 
}

@Injectable()
export class ConsoleService {

    messages: Message[] = [];
    messageObserver: Observer<Message>;
    messageObservable: Observable<Message> = new Observable(observer => this.messageObserver = observer);
    open: boolean = false;
    deployed: boolean = false;
    readonly MAX: number = 200;

    constructor(private msgBoxService: MsgBoxService) {}

    public log(type: MsgType, txt: string, details?: string[]) {
        const dateStr: string = formatDate(new Date(), 'HH:mm', 'en');
        const message: Message = new Message(type, dateStr + ' - ' + txt, details); 
        this.messages.unshift(message);
        this.messageObserver.next(message);
        if (this.messages.length > this.MAX) {
            this.messages.splice(this.MAX);
        }
        this.msgBoxService.log(type, txt);
    } 
}

