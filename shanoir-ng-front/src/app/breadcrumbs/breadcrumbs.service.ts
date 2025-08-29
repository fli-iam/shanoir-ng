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

import { LocationStrategy } from '@angular/common';
import { Injectable, OnDestroy } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { NavigationEnd, Router } from '@angular/router';
import { BehaviorSubject, Subject, Subscription } from 'rxjs';
import { ImportMode } from '../import/import.component';
import {SuperPromise} from "../utils/super-promise";

@Injectable()
export class BreadcrumbsService implements OnDestroy {

    steps: Step[] = [];

    private popFoundedStepIndex: number;
    public currentStepIndex: number;
    private ignoreNavigationEnd: boolean = false;
    private subscriptions: Subscription[] = [];
    onUpdateSteps: BehaviorSubject<{steps: Step[], operation?: 'ADD' | 'REMOVE' | 'MILESTONE' | 'FOCUS'}> = new BehaviorSubject({steps: this.steps});
    private nextPrefill: { field: string; value: any; readOnly: boolean; };

    constructor(
        private router: Router,
        private locationStrategy: LocationStrategy,
        private titleService: Title) {

        locationStrategy.onPopState((event: PopStateEvent) => {
            /* detect back & forward browser events and find the target step using its timestamp */
            for (let i=this.steps.length-1; i>=0; i--) {
                if(this.steps[i].timestamp == event.state) {
                    this.popFoundedStepIndex = i;
                    break;
                }
            }
            // this.saveSession();
        });

        this.subscriptions.push(router.events.subscribe(event => {
            if (event instanceof NavigationEnd
                // navigating inside a page is not changing page
                && event.url?.split('#')[0] != this.currentStep?.route?.split('#')[0]) {

                if(this.ignoreNavigationEnd) {
                    this.ignoreNavigationEnd = false;
                    return;
                }
                const timestamp: number = new Date().getTime();
                if (this.router.getCurrentNavigation().extras?.replaceUrl) {
                    this.steps.pop();
                    this.onUpdateSteps.next({steps: this.steps, operation: 'REMOVE'});
                }
                if (this.popFoundedStepIndex != undefined && this.popFoundedStepIndex != null && this.popFoundedStepIndex >= 0 && this.popFoundedStepIndex < this.steps.length) {
                    this.focusStep(this.popFoundedStepIndex);
                    this.currentStepIndex = this.popFoundedStepIndex;
                    locationStrategy.replaceState(this.steps[this.popFoundedStepIndex].timestamp, 'todo', this.router.url, '');
                } else {
                    this.removeStepsAfter(this.currentStepIndex);
                    this.steps.push(new Step(null, this.router.url, timestamp));
                    this.onUpdateSteps.next({steps: this.steps, operation: 'ADD'});
                    this.currentStepIndex = this.steps.length - 1;
                    locationStrategy.replaceState(timestamp, 'todo', this.router.url, '');
                }
                this.popFoundedStepIndex = null;
                this.currentStep.waitStep = null;
                this.currentStep.addPrefilled(this.nextPrefill?.field, this.nextPrefill?.value, this.nextPrefill?.readOnly);
                this.nextPrefill = null;
            }
        }));
    }

    ngOnDestroy(): void {
        this.subscriptions?.forEach(s => s.unsubscribe());
    }

    private focusStep(index: number) {
        for (let i=index; i>=0; i--) {
            this.steps[i].disabled = false;
            if (this.steps[i].milestone) break;
        }
        for (let i=index+1; i<this.steps.length; i++) {
            this.steps[i].disabled = true;
        }
        this.onUpdateSteps.next({steps: this.steps, operation: 'FOCUS'});
    }

    public nameStep(label: string) {
        setTimeout(() => {
            this.currentStep.label = label;
            this.titleService.setTitle('Shanoir' + (label ? ' - ' + label : ''));
        });
    }

    public markMilestone() {
        setTimeout(() => {
            this.processMilestone();
        });
    }

    public currentStepAsMilestone(label?: string) {
        this.processMilestone(label);
    }

    private processMilestone(label?: string) {
        this.currentStep.milestone = true;
        if (label) this.currentStep.label = label;
        let update: boolean = false;
        for (let i=0; i<this.currentStepIndex; i++) {
            this.steps[i].disabled = true;
            update = true;
        }
        if (update) {
            this.onUpdateSteps.next({steps: this.steps, operation: 'MILESTONE'});
        }
    }

    public goToStepIndex(index: number) {
        history.go(index - this.currentStepIndex);
    }

    public goToStep(step: Step) {
        const index: number = this.steps.findIndex(s => s.id == step.id);
        this.goToStepIndex(index);
    }

    private removeStepsAfter(index: number) {
        this.steps = this.steps.slice(0, index + 1);
        this.onUpdateSteps.next({steps: this.steps, operation: 'REMOVE'});
        if (this.currentStep) {
            this.currentStep.disabled = false;
            this.currentStep.resetWait();
        }
    }

    public goBack(nb?: number) {
        if (nb == undefined) nb = 1;
        else if (nb == null || nb <= 0) return;
        history.go(-1 * nb);
    }

    public get currentStep(): Step {
        return this.steps[this.currentStepIndex];
    }

    public get previousStep(): Step {
        if (this.currentStepIndex < 1) return;
        return this.steps[this.currentStepIndex - 1];
    }

    public isPreviousStateAwaiting(): boolean {
        return this.previousStep.isWaitingFor(this.currentStep);
    }

    public isImporting(): boolean {
        for (let i=this.currentStepIndex; i>=0; i--) {
            if (this.steps[i].importStart) return true;
            else if (this.steps[i].milestone) return false;
        }
        return false;
    }

    public findImportMode(): ImportMode {
        for (let i=this.currentStepIndex; i>=0; i--) {
            if (this.steps[i].importStart) return this.steps[i].importMode;
        }
        return null;
    }

    public addNextStepPrefilled(field: string, value: any, readOnly: boolean = false) {
        this.nextPrefill = { field, value, readOnly };
    }

}

export class Step {

    constructor(
        public label: string,
        public route: string,
        public timestamp: number) {}

    public id = new Date().getTime();
    public subscribers: number = 0;
    public disabled: boolean = false;
    public displayWaitStatus: boolean = true;
    public prefilled: { field: string, value: SuperPromise<any>}[] = [];

    private resolvedPrefilledData: { [field: string]: {value: any, readonly?: boolean} } = {};

    public waitStep: Step;
    private onSaveSubject: Subject<any> = new Subject<any>();
    public milestone: boolean = false;
    public entity: any;
    public data: any = {};
    public importStart: boolean = false;
    public importMode: ImportMode;

    public uniqueId: number = Math.floor(Math.random() * 1000000);

    private onSave(): Subject<any> {
        this.subscribers++;
        return this.onSaveSubject;
    }

    public notifySave(entity: any) {
        this.onSaveSubject.next(entity);
        this.subscribers = 0;
    }

    public hasSubscribers(): boolean {
        return this.subscribers > 0;
    }

    public isWaitingFor(step: Step): boolean {
        return this.waitStep && step.route == this.waitStep.route;
    }

    public waitFor(step: Step, displayWaitStatus: boolean = true): Subject<any> {
        if (displayWaitStatus != undefined) this.displayWaitStatus = displayWaitStatus;
        this.waitStep = step;
        return step.onSave();
    }

    public get isWaiting(): boolean {
        return this.waitStep != null && this.waitStep != undefined;
    }

    public isPrefilled(field: string): boolean {
        return this.prefilled.filter(obj => obj.field == field).length > 0;
    }

    public addPrefilled(field: string, value: any, readOnly: boolean = false) {
        const found = this.prefilled.find(obj => obj.field === field);
        if (found) {
            this.resolvedPrefilledData[field] = {value: value, readonly: readOnly};
            found.value.resolve(value);
        } else {
            const superPro = new SuperPromise<{value: any, readonly?: boolean}>();
            this.prefilled.push({ field, value: superPro });
            this.resolvedPrefilledData[field] = {value: value, readonly: readOnly};
            superPro.resolve(value);
        }
    }

    public getPrefilled(field: string): Promise<{value: any, readonly?: boolean}> {
        const found = this.prefilled.find(obj => obj.field === field);
        if (found) {
            return SuperPromise.timeoutPromise().then(() => {
                return this.resolvedPrefilledData[field];
            });
        } else {
            const superPro = new SuperPromise<{value: any, readonly?: boolean}>();
            this.prefilled.push({ field, value: superPro });
            return superPro;
        }
    }

    public getPrefilledKeys(): string[] {
        return Object.entries(this.resolvedPrefilledData).map(([key, value]) => key);
    }

    getPrefilledValue(field: string): Promise<any> {
        return this.getPrefilled(field).then(res => res?.value);
    }

    public resetWait() {
        this.waitStep = null;
    }
}
