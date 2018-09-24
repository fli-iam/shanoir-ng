import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Entity } from '../shared/components/entity/entity.interface';

@Injectable()
export class BreadcrumbsService {

    public steps: Step[] = [];

    constructor(private router: Router) {}

    public addStep(step: Step) {
        this.steps.push(step);
    }

    public reset() {
        this.steps = [];
    }

    public clickStep(index: number) {
        let step = this.steps[index];
        this.steps = this.steps.slice(0, index);
        console.log(step.entity)
        this.router.navigate([step.route, {fragment: step.entity}])
    }

}

export class Step {

    constructor(
        public label: string,
        public route: string,
        public entity?: Entity
    ) {}

}