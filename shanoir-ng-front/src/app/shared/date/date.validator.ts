import { Directive } from '@angular/core';
import { NG_VALIDATORS, ValidatorFn, FormControl, Validator } from '@angular/forms';
@Directive({
    selector: '[validDate][ngModel]',
    providers: [
        {
            provide: NG_VALIDATORS,
            useExisting: DateValidator,
            multi: true
        }
    ]
})
export class DateValidator implements Validator {
    validator: ValidatorFn;
    constructor() {
        this.validator = this.dateValidator();
    }
    validate(c: FormControl) {
        return this.validator(c);
    }
    dateValidator(): ValidatorFn {
        return (control: FormControl) => {
            return control.value == 'invalid' ? {validDate: {valid: false}} : null;
        }
    }
}