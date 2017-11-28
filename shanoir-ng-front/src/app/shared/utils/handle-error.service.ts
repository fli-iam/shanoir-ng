import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class HandleErrorService {

    constructor () {}

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
        console.error(errMsg);
        return Observable.throw(errMsg);
    }
}  