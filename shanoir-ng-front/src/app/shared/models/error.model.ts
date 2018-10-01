export class ShanoirError {
    constructor(
        public code: number,
        public details: any,
        public message: string
    ) {}

    public hasFieldError(field: string, code: string): boolean {
        if (this.details && this.details.fieldErrors && this.details.fieldErrors[field]) {
            for (let error of this.details.fieldErrors[field]) {
                if (error.code == code) return true;
            }
        }
    }
}