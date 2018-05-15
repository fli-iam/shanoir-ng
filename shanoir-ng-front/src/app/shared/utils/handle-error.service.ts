import { Injectable, ErrorHandler } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { MsgBoxService } from '../msg-box/msg-box.service';
import { GuiError } from '../models/error.model';

@Injectable()
export class HandleErrorService implements ErrorHandler {

    constructor (private msgb: MsgBoxService) { }

    public extractData(res: Response) {
        let body = res.json();
        return body.data || { };
    }

    public handleError(error: Response | any) {
        let techMsg: string = error.message ? error.message : error.toString();
        let userMsg: string = 'An unexpected error occured';

        if (error instanceof Response) {
            const body = error.json() || '';
            techMsg = '';
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
            // TODO : userMsg = techMsg;
        } 

        if (error.promise && error.rejection) {
            techMsg = error.rejection;
            userMsg = error.rejection.guiMsg;
        }

        if (error.guiMsg) {
            userMsg = error.guiMsg;
        }

        console.error(techMsg);
        this.msgb.log('error', userMsg);
    }
}  