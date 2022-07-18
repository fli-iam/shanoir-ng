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
import { formatDate } from '@angular/common';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { StatusCodes } from 'http-status-codes';
import { Observable } from 'rxjs/Observable';
import { catchError, finalize, tap } from 'rxjs/operators';

import { ConsoleService } from '../shared/console/console.service';
import { LoaderService } from '../shared/loader/loader.service';


@Injectable()
export class ShanoirHttpInterceptor implements HttpInterceptor {

    constructor(private loaderService: LoaderService, private consoleService: ConsoleService) {}

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (request.url.endsWith('shanoir-ng/users/tasks')) {
            return next.handle(request);
        } else {
            this.loaderService.startLoader();
            return next.handle(request).pipe(
                finalize(() => {
                    this.loaderService.stopLoader();
                })
            );
        }
    }

}