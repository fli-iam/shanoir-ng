import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Entity } from '../shared/components/entity/entity.abstract';
import { Subject } from 'rxjs';
import { LocationStrategy } from '@angular/common';

@Injectable()
export class BreadcrumbsService {

    public steps: Step[] = [];
    private savedStep: Step;

    constructor(
            private router: Router, 
            private location: LocationStrategy) {
                
        location.onPopState(() => {
            this.notifyBeforeBack();
        });
    }

    public addStep(label: string) {
        let step = new Step(label, this.router.url);
        if (this.lastStep && step.route == this.lastStep.route) return;
        if (this.beforeLastStep && step.route == this.beforeLastStep.route) {
            this.removeStepsAfter(this.nbSteps - 2);
            this.savedStep = this.lastStep;
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
            this.lastStep.disabled = false;
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
        public entity?: Entity
    ) { }

    private subscribers: number = 0;
    private onSaveSubject: Subject<Entity> = new Subject<Entity>();
    private waitStep: Step;
    public disabled: boolean = false;
    private displayWaitStatus: boolean = true;
    private prefilled: any[] = [];

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