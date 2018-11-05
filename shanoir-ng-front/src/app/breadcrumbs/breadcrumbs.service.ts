import { LocationStrategy } from '@angular/common';
import { Injectable } from '@angular/core';
import { Event, NavigationEnd, NavigationStart, Router } from '@angular/router';
import { Subject } from 'rxjs';

import { Entity } from '../shared/components/entity/entity.abstract';

@Injectable()
export class BreadcrumbsService {

    public steps: Step[] = [];
    
    private popFoundedStepIndex: number;
    public replace: boolean = false;
    public currentStepIndex: number;
    private nextLabel: string;
    private nextMilestone: boolean = false;

    constructor(
            private router: Router, 
            private locationStrategy: LocationStrategy) {
                
        locationStrategy.onPopState((event: PopStateEvent) => {
            /* detect back & forward browser events and find the target step using its timestamp */
            for (let i=this.steps.length-1; i>=0; i--) {
                if(this.steps[i].timestamp == event.state) {
                    this.popFoundedStepIndex = i;
                    break;
                }
            }
        });

        router.events.subscribe( (event: Event) => {
            if (event instanceof NavigationEnd) {
                const timestamp: number = new Date().getTime();
                if (this.replace) this.steps.pop();
                if (this.popFoundedStepIndex != undefined && this.popFoundedStepIndex != null && this.popFoundedStepIndex >= 0 && this.popFoundedStepIndex < this.steps.length) {
                    this.focusStep(this.popFoundedStepIndex);
                    this.currentStepIndex = this.popFoundedStepIndex;
                    locationStrategy.replaceState(this.steps[this.popFoundedStepIndex].timestamp, 'todo', this.router.url, '');
                } else {
                    this.removeStepsAfter(this.currentStepIndex);
                    this.steps.push(new Step(this.nextLabel, this.router.url, timestamp));
                    this.currentStepIndex = this.steps.length - 1;
                    locationStrategy.replaceState(timestamp, 'todo', this.router.url, '');
                }
                if (this.nextMilestone) this.processMilestone();
                this.nextMilestone = false;
                this.nextLabel = null;
                this.popFoundedStepIndex = null;
                this.currentStep.waitStep = null;
            }
        });

    }

    private focusStep(index: number) {
        for (let i=index; i>=0; i--) {
            this.steps[i].disabled = false;
            if (this.steps[i].milestone) break;
        }
        for (let i=index+1; i<this.steps.length; i++) {
            this.steps[i].disabled = true;
        }
    }

    public nameStep(label: string) {
        this.nextLabel = label;
    }

    public markMilestone() {
        this.nextMilestone = true;
    }
    
    private processMilestone() {
        this.currentStep.milestone = true;
        for (let i=0; i<this.currentStepIndex; i++) {
            this.steps[i].disabled = true;
        }
    }

    public goToStep(index: number) {
        history.go(index - this.currentStepIndex);
    }

    private removeStepsAfter(index: number) {
        this.steps = this.steps.slice(0, index + 1);
        if (this.currentStep) {
            this.currentStep.disabled = false;
            this.currentStep.resetWait();
        }
    }

    public goBack() {
        history.go(-1);
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

}

export class Step {

    constructor(
            public label: string,
            public route: string,
            public timestamp: number) {
    }

    // static parse(str: string): Step {
    //     let json: Step = JSON.parse(str);
    //     let step: Step = new Step(json.label, json.route, json.entity);
    //     step.id = step.id;
    //     step.subscribers = step.subscribers;
    //     step.disabled = step.disabled;
    //     step.displayWaitStatus = step.displayWaitStatus;
    //     step.prefilled = step.prefilled;
    //     return step;
    // }

    // stringify(): string {
    //     console.log('stringify')
    //     let ignoreList: string[] = ['onSaveSubject'];
    //     let replacer = (key, value) => {
    //         if (ignoreList.indexOf(key) > -1) return undefined;
    //         else if (key == 'entity') return (value as Entity).stringify();
    //         else return value;
    //     }
    //     return JSON.stringify(this, replacer);
    // }

    public id = new Date().getTime();
    public subscribers: number = 0;
    public disabled: boolean = false;
    public displayWaitStatus: boolean = true;
    public prefilled: any[] = [];
    public waitStep: Step;
    private onSaveSubject: Subject<Entity> = new Subject<Entity>();
    public milestone: boolean = false;
    public entity: Entity;
    public data: any = {};

    private onSave(): Subject<Entity> {
        this.subscribers++;
        return this.onSaveSubject;
    }

    public notifySave(entity: Entity) {
        this.onSaveSubject.next(entity);
        this.subscribers = 0;
    }

    public hasSubscribers(): boolean {
        return this.subscribers > 0;
    }

    public isWaitingFor(step: Step): boolean {
        return this.waitStep && step.route == this.waitStep.route;
    }

    public waitFor(step: Step, displayWaitStatus: boolean = true): Subject<Entity> {
        if (displayWaitStatus != undefined) this.displayWaitStatus = displayWaitStatus;
        this.waitStep = step;
        return step.onSave();
    }

    public get isWaiting(): boolean {
        return this.waitStep != null && this.waitStep != undefined;
    }

    public addPrefilled(field: string, value: any) {
        this.prefilled.push({field: field, value: value});
    }

    public isPrefilled(field: string): boolean {
        return this.prefilled.filter(obj => obj.field == field).length > 0;
    }

    public getPrefilledValue(field: string): any {
        let found: any[] = this.prefilled.filter(obj => obj.field == field);
        return found && found.length > 0 ? found[0].value : undefined;
    }

    public resetWait() {
        this.waitStep = null;
    }
}