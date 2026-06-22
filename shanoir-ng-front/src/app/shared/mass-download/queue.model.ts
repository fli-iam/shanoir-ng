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

import { BehaviorSubject, Subject } from "rxjs";
import { takeUntil } from "rxjs/operators";

export class Queue {

    private _queue: BehaviorSubject<number> = new BehaviorSubject(1);
    private _nextTicket: number = 1;

    constructor() {
        this._queue.next(1);         
    }

    waitForTurn(): Promise<() => void> {
        const stop: Subject<void> = new Subject<void>();
        const ticket: number = this._nextTicket++;
        return new Promise((resolve,) => {
            // takeUntil(stop) manages the unsubscription
            this._queue.pipe(takeUntil(stop)).subscribe(calledTicket => {
                if (calledTicket == ticket) {
                    stop.next(null);
                    stop.complete();
                    const release = () => {
                        this._queue.next(ticket + 1);
                    };
                    resolve(release);
                }
            });  
        });
    }

}