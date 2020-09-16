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

import { ErrorHandler, Injectable } from '@angular/core';
import { HttpResponse } from '@angular/common/http';

import { MsgBoxService } from '../msg-box/msg-box.service';

@Injectable()
export class HandleErrorService implements ErrorHandler {

    constructor (private msgb: MsgBoxService) { }

    public handleError(error: Response | any) {
        try {
            let techMsg: string = error.message ? error.message : null;
            let userMsg: string = 'An unexpected error occured';
    
            if (error instanceof Response) {
                techMsg = this.getMsgFromBackendValidation(error);
            } 
    
            if (error.guiMsg) {
                userMsg = error.guiMsg;
            }
            
            if (techMsg) console.error(techMsg);
            console.error(error);
            this.msgb.log('error', userMsg);

        } catch (error) {
            console.error('Error handler failed : ', error);
        }
    }
    
    private getMsgFromBackendValidation(error: Response | any): string {
        const body = error.json() || '';
        let techMsg = '';
        techMsg = "[" + body.code + "]: " + body.message;
        if (body.details) {
            let errDetails = body.details.fieldErrors || '';
            for (var errKey in errDetails) {
                techMsg += "; " + errKey + " should be ";
                var errDetailsByKey = errDetails[errKey][0];
                for (var errDetail in errDetailsByKey) {
                    if (errDetail === "code")
                    techMsg += errDetailsByKey[errDetail];
                }
            }
        }
        return techMsg;
    }
}  