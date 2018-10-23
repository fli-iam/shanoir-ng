import { findIndex } from "rxjs/operator/findIndex";

export class ShanoirError {
    public code: number;
    public details: any;
    public message: string;
    
    constructor(reason: any) {
        this.code = reason.error.code;
        this.details = reason.error.details;
        this.message = reason.error.message;
    }

    public hasFieldError(field: string, code: string, value?: string): boolean {
        if (this.details && this.details.fieldErrors && this.details.fieldErrors[field]) {
            for (let error of this.details.fieldErrors[field]) {
                if (error.code == code && (!value || error.givenValue == value)) return true;
            }
        }
    }
}