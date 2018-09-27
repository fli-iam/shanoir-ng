import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Entity } from '../shared/components/entity/entity.interface';
import { Subject } from 'rxjs';

@Injectable()
export class BreadcrumbsService {

    public steps: Step[] = [];
    public savedStep: Step;

    constructor(private router: Router) {}

    public addStep(step: Step) {
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
        this.steps = this.steps.slice(0, index);
    }

    public goBack() {
        this.goToStep(this.nbSteps - 2);
    }

    public disableLastStep() {
        this.lastStep.disabled = true;
    }

    public notifyBeforeBack() {
        if (this.nbSteps > 0) this.steps.pop();
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
    ) {}

    private subscribers: number = 0;
    private onSaveSubject: Subject<Entity> = new Subject<Entity>();
    private waitStep: Step;
    public disabled: boolean = false;

    public onSave(): Subject<Entity> {
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

    public waitFor(step: Step): Subject<Entity> {
        this.waitStep = step;
        return step.onSave();
    }

    public get isWaiting(): boolean {
        return this.waitStep != null && this.waitStep != undefined;
    }
}