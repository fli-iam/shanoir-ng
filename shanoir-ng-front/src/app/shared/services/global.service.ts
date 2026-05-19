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

import { Injectable, ElementRef, OnDestroy } from '@angular/core';
import { Observable , Subject, Subscription, fromEvent } from 'rxjs';
import { filter } from 'rxjs/operators';
import { LocationStrategy } from '@angular/common';
import { Router } from '@angular/router';

@Injectable()
export class GlobalService implements OnDestroy {
    
    public onGlobalClick: Observable<Event>;
    public onGlobalMouseUp: Observable<Event>;
    private _onNavigate: Subject<any> = new Subject();
    private subscription: Subscription;
    
    constructor(locationStrategy: LocationStrategy, router: Router) {
        locationStrategy.onPopState((event: PopStateEvent) => {
            this._onNavigate.next(event);
        });
        this.subscription = router.events.subscribe(event => {
            this._onNavigate.next(event);
        })
    }

    ngOnDestroy(): void {
        this.subscription?.unsubscribe();
    }
    
    registerGlobalClick(rootElement: ElementRef) {
        this.onGlobalClick = fromEvent(rootElement.nativeElement, 'click');
        this.onGlobalMouseUp = fromEvent(rootElement.nativeElement, 'mouseup');
    }
    
    onClickOutside(elementRef: ElementRef<any>) {
        return this.onGlobalClick.pipe(filter(clickEvent => !elementRef.nativeElement.contains(clickEvent.target)));
    }

    get onNavigate(): Observable<any> {
        return this._onNavigate.asObservable();
    }
}