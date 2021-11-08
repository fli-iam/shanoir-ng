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