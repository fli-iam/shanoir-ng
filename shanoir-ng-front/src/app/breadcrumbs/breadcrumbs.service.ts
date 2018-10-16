import { LocationStrategy } from '@angular/common';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Subject } from 'rxjs';

import { Entity } from '../shared/components/entity/entity.abstract';

@Injectable()
export class BreadcrumbsService {

    public steps: Step[] = [];
    public savedStep: Step;

    constructor(
            private router: Router, 
            private locationStrategy: LocationStrategy) {
                
        locationStrategy.onPopState(() => {
            this.notifyBeforeBack();
        });

        // router.events.subscribe( (event: Event) => {
        //     if (event instanceof NavigationStart) {
        //         let bcToBeStored =  {steps: this.steps, savedStep: this.savedStep};
        //         //console.log(JSON.stringify(state), JSON.stringify(state).length);
        //         sessionStorage.setItem('breadcrumbs', JSON.stringify(bcToBeStored));
        //     }
        // });
    }

    public addStep(label: string) {
        let step = new Step(label, this.router.url);
        if (this.lastStep && step.route == this.lastStep.route) {
            this.removeStepsAfter(this.nbSteps - 2);
        }
        if (this.beforeLastStep && step.route == this.beforeLastStep.route) {
            this.removeStepsAfter(this.nbSteps - 3);
        }
        this.steps.push(step);
    }

    public reset() {
        this.steps = [];
    }

    public goToStep(index: number) {
        this.savedStep = this.steps[index];
        this.removeStepsAfter(index);
        this.savedStep.disabled = false;
        this.router.navigate([this.savedStep.route]);
    }

    private removeStepsAfter(index: number) {
        this.steps = this.steps.slice(0, index + 1);
        if (this.nbSteps > 0) {
            // this.lastStep.disabled = false;
            this.lastStep.resetWait();
        }
    }

    public goBack() {
        this.goToStep(this.nbSteps - 2);
    }

    public disableLastStep() {
        this.lastStep.disabled = true;
    }

    public notifyBeforeBack() {
        if (this.nbSteps > 0) {
            this.steps.pop();
            if (this.nbSteps > 0) {
                this.lastStep.disabled = false;
                this.lastStep.resetWait();
            }
        }
    }

    public entityToReload(): boolean {
        return this.savedStep && this.savedStep.entity && this.savedStep.route == this.router.url;
    }

    public reloadSavedEntity<T extends Entity>(): T {
        return this.savedStep.entity as T
    }

    public get lastStep(): Step {
        return this.steps[this.nbSteps - 1];
    }

    public get beforeLastStep(): Step {
        return this.steps[this.nbSteps - 2];
    }

    public get nbSteps(): number {
        return this.steps.length;
    }

    public isPreviousStateAwaiting(): boolean {
        return this.beforeLastStep.isWaitingFor(this.lastStep);
    }

}

export class Step {

    constructor(
            public label: string,
            public route: string,
            public entity?: Entity) {
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