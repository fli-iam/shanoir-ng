import { Injectable, ErrorHandler } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { MsgBoxService } from '../msg-box/msg-box.service';
import { GuiError } from '../models/error.model';

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