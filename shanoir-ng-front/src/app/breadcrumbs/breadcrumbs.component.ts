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

import { AfterViewChecked, AfterViewInit, Component, ElementRef, HostListener, Input, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { BreadcrumbsService, Step } from './breadcrumbs.service';
import { Subject, Subscription } from 'rxjs';
import { WaitBurstEnd } from '../utils/waitBurstEnd';
import { take } from 'rxjs/operators';

@Component({
    selector: 'breadcrumbs',
    templateUrl: 'breadcrumbs.component.html',
    styleUrls: ['breadcrumbs.component.css']
})

export class BreadcrumbsComponent implements AfterViewInit, OnDestroy, AfterViewChecked {

    private _nbDisplayedSteps: number = 0;
    private subscriptions: Subscription[] = [];
    private onViewChecked: Subject<void> = new Subject();
    private onResizeEnd: WaitBurstEnd;
    private onViewCheck: WaitBurstEnd;
    private checkWidthBurst: WaitBurstEnd;
    private steps: Step[];
    protected displayedSteps: Step[];

    constructor(
        private service: BreadcrumbsService,
        private router: Router,
        private elementRef: ElementRef) { 

            this.onResizeEnd = new WaitBurstEnd(this.checkWidth.bind(this), 500);
            this.onViewCheck = new WaitBurstEnd(() => this.onViewChecked.next(), 300);
            this.checkWidthBurst = new WaitBurstEnd(this._checkWidth.bind(this), 200);

            this.subscriptions.push(service.onUpdateSteps.subscribe(ret => {
                this.steps = ret.steps;
                setTimeout(() => {
                    if (ret.operation == 'ADD') {
                        this.nbDisplayedSteps++;
                    }
                    this.displayedSteps = ret.steps?.slice(-this.nbDisplayedSteps);
                    this.checkWidth();
                });
            }));
    }

    ngAfterViewChecked(): void {
        this.onViewCheck.fire();
    }

    ngOnDestroy(): void {
        this.subscriptions.forEach(s => s.unsubscribe());
    }

    ngAfterViewInit(): void {
        this.checkWidth();
    }

    get nbDisplayedSteps(): number {
        return this._nbDisplayedSteps;
    }

    set nbDisplayedSteps(nb: number) {
        this._nbDisplayedSteps = nb;
        setTimeout(() => {
            this.displayedSteps = this.steps?.slice(-this.nbDisplayedSteps);
        });
    }

    clickStep(step: Step) {
        this.service.goToStep(step);
    }    

    goHome() {
        this.router.navigate(['/home']);
    }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        this.onResizeEnd.fire();
    }

    private checkWidth() {
        this.checkWidthBurst.fire();
    }

    private _checkWidth() {
        let componentWidth: number = this.elementRef.nativeElement.offsetWidth;
        let listWidth: number = this.elementRef.nativeElement.scrollWidth;
        let nbSteps: number = this.steps.filter(s => !s.disabled)?.length;
        if (!this.nbDisplayedSteps) this.nbDisplayedSteps = nbSteps;
        if (listWidth > componentWidth) { // if overflow, reduce
            this.reduceUntilFit();
        } else if (this.nbDisplayedSteps > 0 && this.nbDisplayedSteps < nbSteps) { // else, try to expand
            this.tryToExpand();
        }
    }

    private tryToExpand() {
        if (this.nbDisplayedSteps >= this.steps.filter(s => !s.disabled)?.length) return;
        this.nbDisplayedSteps++;
        this.onViewChecked.pipe(take(1)).subscribe(() => {
            let componentWidth: number = this.elementRef.nativeElement.offsetWidth;
            let listWidth: number = this.elementRef.nativeElement.scrollWidth;
            if (listWidth > componentWidth) { // if overflow, finally reduce
                this.nbDisplayedSteps--;
            } else { // else continue
                this.tryToExpand();
            }
        });
    }

    private reduceUntilFit() {
        if (this.nbDisplayedSteps <= 0) return;
        this.nbDisplayedSteps--;
        this.onViewChecked.pipe(take(1)).subscribe(() => {
            let componentWidth: number = this.elementRef.nativeElement.offsetWidth;
            let listWidth: number = this.elementRef.nativeElement.scrollWidth;
            if (listWidth > componentWidth) { // if overflow, reduce again
                this.reduceUntilFit();
            }
        });
    }

    @HostListener('document:keypress', ['$event']) onKeydownHandler(event: KeyboardEvent) {
        if (event.key == '²') {
            console.log('breadcrumbs', this.steps);
        }
    }
}