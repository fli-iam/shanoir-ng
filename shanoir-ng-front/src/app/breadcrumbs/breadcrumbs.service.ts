import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Entity } from '../shared/components/entity/entity.interface';

@Injectable()
export class BreadcrumbsService {

    public steps: Step[] = [];
    public lastStep: Step;

    constructor(private router: Router) {}

    public addStep(step: Step) {
        this.steps.push(step);
    }

    public reset() {
        this.steps = [];
    }

    public clickStep(index: number) {
        this.lastStep = this.steps[index];
        this.steps = this.steps.slice(0, index);
        this.router.navigate([this.lastStep.route])
    }

    public notifyBack() {
        if (this.steps.length > 0) {
            const index = this.steps.length - 2;
            this.lastStep = this.steps[index];
            this.steps = this.steps.slice(0, index);
        }
    }

    public entityToReload(): boolean {
        return this.lastStep && this.lastStep.entity && this.lastStep.route == this.router.url;
    }

}

export class Step {

    constructor(
        public label: string,
        public route: string,
        public entity?: Entity
    ) {}

}