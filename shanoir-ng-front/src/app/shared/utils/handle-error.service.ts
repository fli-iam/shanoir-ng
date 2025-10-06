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
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorHandler, Injectable } from '@angular/core';
import { StatusCodes } from 'http-status-codes';

import { ConsoleService } from '../console/console.service';



@Injectable()
export class HandleErrorService implements ErrorHandler {

    constructor (private consoleService: ConsoleService) { }

    public handleError(error: any) {
        console.log(error)
        try {
            if (error instanceof HttpErrorResponse) {
                this.handleHttpError(error);
            } else {
                console.error(error);
                this.handleDefaultError(error);
            }
        } catch (error) {
            console.error('Error handler failed : ', error);
        }
    }

    private handleHttpError(error: HttpErrorResponse) {
        try {
            const details: string[] = [
                formatDate(new Date(), 'yyyy-MM-dd HH:mm:ss', 'en'),
                '[' + error.status + '] ' + this.getStatus(error.status),
                error.url,
                ((error.error?.message && error.error.message != '') ? 'message : ' + error.error.message : 'unknown cause')
            ];
            if(error.error instanceof Blob) {
                error.error.text().then(text => {
                    const msg = (JSON.parse(text).message);
                    this.consoleService.log('error', msg, details);
                });
            } else {
                //handle regular json error - useful if you are offline
                const msg: string = 'Error from ' + this.extractServerNameFromUrl(error.url) + ' server';
                this.consoleService.log('error', msg, details);
            }
        } catch (error) {
            console.error(error);
            throw new Error('Error handler failed, cause above');
        }
    }

    private handleDefaultError(error: any) {
        if (error?.message?.startsWith('No activity within')) {
            return;
        }
        try {
            let msg: string = 'Error' 
            if (error.name != 'Error') msg += ' : ' + error.name;
            const details: string[] = [error.message];
            this.consoleService.log('error', msg, details);
        } catch (error) {
            console.error(error);
            throw new Error('Error handler failed, cause above');
        }
    }

    private getStatus(code: number): string {
        return Object.keys(StatusCodes).find(status => StatusCodes[status] === code);
    }

    private extractServerNameFromUrl(url: string) {
        const urlArr: string[] = url.split('/');
        return urlArr[urlArr.findIndex(str => str == 'shanoir-ng') + 1];
    }
}  