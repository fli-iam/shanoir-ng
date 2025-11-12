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

import { ApplicationRef, Component, HostBinding, Injector, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { slideDown } from '../animations/animations';

import { ConsoleService, Message } from './console.service';
import { NgIf, NgFor } from '@angular/common';

@Component({
    selector: 'shanoir-console',
    templateUrl: './console.component.html',
    styleUrls: ['./console.component.css'],
    animations: [slideDown],
    imports: [NgIf, NgFor]
})
export class ConsoleComponent implements OnDestroy {

    private _open: boolean = false;
    @HostBinding('class.deployed') deployed: boolean;
    contentOpen: boolean = this._open;
    messages: Message[] = [];
    private closeTimeout: any;
    private appRef: Promise<ApplicationRef> = new Promise(() => { return});
    private subscription: Subscription;

    constructor(public consoleService: ConsoleService, private injector: Injector) {
        this.messages = consoleService.messages.slice();
        this._open = consoleService.open;
        this.deployed = consoleService.deployed;
        this.contentOpen = this._open;
        this.subscription = consoleService.messageObservable.subscribe(this.processNewMsg);
        setTimeout(() => this.appRef = Promise.resolve(this.injector.get(ApplicationRef)));
    }

    private processNewMsg = (message: Message) => {
        if (this.messages.length > 0 && this.messages[0].txt == message.txt 
                && (
                    (!this.messages[0].details && !message.details)
                    || (
                        this.messages[0].details && message.details
                        && this.messages[0].details.length == message.details.length 
                        && this.messages[0].details.every((value, index) => value == message.details[index])
                    )
                )
        ){
            this.messages[0].nb ++;
        } else {
            this.messages.unshift(message);
        }
        // if (!this.open) {
        //     this.open = true; 
        //     this.closeTimeout = this.setCloseTimeout();
        // } else if (this.closeTimeout) {
        //     this.closeTimeout = this.setCloseTimeout();
        // }
        if (this.messages.length > this.consoleService.MAX) {
            this.messages.splice(this.consoleService.MAX);
        }
    }

    private setCloseTimeout(): any {
        clearTimeout(this.closeTimeout);
        return setTimeout(() => {
            this.open = false;
            this.closeTimeout = null;
        }, 5000);
    }

    ngOnDestroy(): void {
        this.subscription.unsubscribe();
    }

    @HostBinding('class.open') 
    get open(): boolean {
        return this._open;
    }

    set open(open: boolean) {
        if (this._open != open) {
            clearTimeout(this.closeTimeout);
            this._open = open;
            if (!open) setTimeout(() => {
                this.contentOpen = open;
                this.appRef.then(appRef => appRef.tick());
            }, 1000);
            else this.contentOpen = open;
            this.appRef.then(appRef => appRef.tick());
        }
        this.closeTimeout = null;
        this.consoleService.open = this._open;
        this.consoleService.deployed = this.deployed;
    }

    toggleDetails(message: Message) {
        clearTimeout(this.closeTimeout);
        message.detailsOpened = !message.detailsOpened;
        this.appRef.then(appRef => appRef.tick());
    }

}
