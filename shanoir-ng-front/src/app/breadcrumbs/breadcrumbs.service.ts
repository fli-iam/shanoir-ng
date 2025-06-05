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
    private nextLabel: string;
    private nextMilestone: boolean = false;
    private ignoreNavigationEnd: boolean = false;
    private subscriptions: Subscription[] = [];
    onUpdateSteps: BehaviorSubject<{steps: Step[], operation?: 'ADD' | 'REMOVE' | 'MILESTONE'}> = new BehaviorSubject({steps: this.steps});

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
                    this.steps.push(new Step(this.nextLabel, this.router.url, timestamp));
                    this.onUpdateSteps.next({steps: this.steps, operation: 'ADD'});
                    this.currentStepIndex = this.steps.length - 1;
                    locationStrategy.replaceState(timestamp, 'todo', this.router.url, '');
                    titleService.setTitle('Shanoir' + (this.nextLabel ? ' - ' + this.nextLabel : ''));
                }
                if (this.nextMilestone) this.processMilestone();
                this.nextMilestone = false;
                this.nextLabel = null;
                this.popFoundedStepIndex = null;
                this.currentStep.waitStep = null;
                // this.saveSession();
            }
        }));
        // this.loadSession();
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
        this.onUpdateSteps.next({steps: this.steps, operation: 'MILESTONE'});
    }

    public nameStep(label: string) {
        this.nextLabel = label;
        // this.saveSession();
    }

    public markMilestone() {
        this.nextMilestone = true;
        // this.saveSession();
    }

    public resetMilestone() {
        this.nextMilestone = false;
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

    // public saveSession() {
    //     let stepsJSON = [];
    //     for(let step of this.steps) {
    //         stepsJSON.push(step.save())
    //     }

    //     sessionStorage.setItem('breadcrumbsData', JSON.stringify({
    //         steps: stepsJSON,
    //         popFoundedStepIndex: this.popFoundedStepIndex,
    //         replace: this.replace,
    //         currentStepIndex: this.currentStepIndex,
    //         nextLabel: this.nextLabel,
    //         nextMilestone: this.nextMilestone }));
    // }

    // public loadSession() {
    //     let json = JSON.parse(sessionStorage.getItem('breadcrumbsData'));
    //     if(json == null) {
    //         return;
    //     }
    //     this.popFoundedStepIndex = json.popFoundedStepIndex;
    //     this.replace = json.replace;
    //     this.currentStepIndex = json.currentStepIndex;
    //     this.nextLabel = json.nextLabel;
    //     this.nextMilestone = json.nextMilestone;
    //     this.steps = [];
    //     for(let step of json.steps) {
    //         this.steps.push(Step.load(step));
    //     }

    //     this.titleService.setTitle('Shanoir' + (this.nextLabel ? ' - ' + this.nextLabel : ''));
    //     this.ignoreNavigationEnd = true;
    // }

    public findImportMode(): ImportMode {
        for (let i=this.currentStepIndex; i>=0; i--) {
            if (this.steps[i].importStart) return this.steps[i].importMode;
        }
        return null;
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
    public prefilled: { field: string, value: Promise<any>}[] = [];
    public waitStep: Step;
    private onSaveSubject: Subject<any> = new Subject<any>();
    public milestone: boolean = false;
    public entity: any;
    public data: any = {};
    public importStart: boolean = false;
    public importMode: ImportMode;

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

    public addPrefilled(field: string, value: any) {
        let found: any[] = this.prefilled.filter(obj => obj.field == field);
        if (found.length > 0) {
            return found[0].value.resolve(value);
        } else {
            let superPro = new SuperPromise();
            this.prefilled.push({field: field, value: value});
            //this.addPrefilled(field, superPro);
            superPro.resolve(value);
        }
    }

    public isPrefilled(field: string): boolean {
        return this.prefilled.filter(obj => obj.field == field).length > 0;
    }

    public async getPrefilledValue(field: string): Promise<any> {
        if (this.isPrefilled(field)) {
            return SuperPromise.timeoutPromise().then( () => {
                let found: any[] = this.prefilled.filter(obj => obj.field == field);
                return found && found.length > 0 ? found[0].value : undefined;
            })
        } else {
            let superPro = new SuperPromise();
            this.addPrefilled(field, superPro);
            return superPro;
        }
    }

    public resetWait() {
        this.waitStep = null;
    }
}
