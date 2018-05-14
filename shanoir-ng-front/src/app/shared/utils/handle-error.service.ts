import { Injectable, ErrorHandler } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { MsgBoxService } from '../msg-box/msg-box.service';

@Injectable()
export class HandleErrorService implements ErrorHandler {

    constructor (private msgb: MsgBoxService) { }

    public extractData(res: Response) {
        let body = res.json();
        return body.data || { };
    }

    public handleError(error: Response | any) {
        let errMsg: string;
        if (error instanceof Response) {
            const body = error.json() || '';
            errMsg= "[" + body.code + "]: " + body.message;
            if (body.details) {
                let errDetails = body.details.fieldErrors || '';
                for (var errKey in errDetails) {
                    errMsg += "; " + errKey + " should be ";
                    var errDetailsByKey = errDetails[errKey][0];
                    for (var errDetail in errDetailsByKey) {
                        if (errDetail === "code")
                        errMsg += errDetailsByKey[errDetail];
                    }
                }
            }
        } else {
            errMsg = error.message ? error.message : error.toString();
        }
        console.log(error.message);
        this.msgb.log('error', errMsg);
        throw errMsg;
    }
}  