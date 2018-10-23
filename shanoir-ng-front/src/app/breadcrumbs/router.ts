import { Injectable } from '@angular/core';
import { NavigationExtras, Router as AngularRouter } from '@angular/router';
import { BreadcrumbsService } from './breadcrumbs.service';

@Injectable()
export class Router {

    constructor(
        private angularRouter: AngularRouter,
        private breadcrumbsService: BreadcrumbsService) {

    }

    navigate(commands: any[], extras?: NavigationExtras): Promise<boolean> {
        if (extras && extras.replaceUrl) {
            this.breadcrumbsService.replace = true;
        }
        return this.angularRouter.navigate(commands, extras).then(_ => this.breadcrumbsService.replace = false);
    }

}