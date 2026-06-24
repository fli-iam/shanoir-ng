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

import { Component, EventEmitter, HostBinding, Injector, OnDestroy, Output } from '@angular/core';
import { Subscription } from 'rxjs';

import { ConsoleService, Message } from './console.service';


@Component({
    selector: 'shanoir-console',
    templateUrl: './console.component.html',
    styleUrls: ['./console.component.css'],
    imports: []
})
export class ConsoleComponent implements OnDestroy {

    private _open: boolean = false;
    @HostBinding('class.deployed') deployed: boolean;
    contentOpen: boolean = this._open;
    messages: Message[] = [];
    private subscription: Subscription;
    @Output() registerToggle: EventEmitter<(open: boolean) => void> = new EventEmitter();
    @Output() consoleOpened: EventEmitter<boolean> = new EventEmitter();
    @Output() consoleDeployed: EventEmitter<boolean> = new EventEmitter();

    constructor(public consoleService: ConsoleService, private injector: Injector) {
        this.messages = consoleService.messages.slice();
        this._open = consoleService.open;
        this.deployed = consoleService.deployed;
        this.contentOpen = this._open;
        this.subscription = consoleService.messageObservable.subscribe(this.processNewMsg);
        setTimeout(() => this.registerToggle.emit(this.toggle.bind(this)), 0);
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
        if (this.messages.length > this.consoleService.MAX) {
            this.messages.splice(this.consoleService.MAX);
        }
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
            this._open = open;
            if (!open) {
                this.contentOpen = open;
            } else this.contentOpen = open;
        }
        this.consoleService.open = this._open;
        this.consoleService.deployed = this.deployed;
        this.consoleOpened.emit(this._open);
    }

    private toggle(open: boolean) {
        this.open = open;
    }

    toggleDeployed(deployed: boolean) {
        this.deployed = deployed;
        this.consoleService.deployed = this.deployed;
        this.consoleDeployed.emit(this.deployed);
    }

    toggleDetails(message: Message) {
        message.detailsOpened = !message.detailsOpened;
    }

}
